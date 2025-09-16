
/**
 * DfaMinimizer
 * -------------
 * This class provides an implementation of DFA minimization using the table-filling algorithm.
 * It identifies and merges equivalent states in a deterministic finite automaton (DFA),
 * resulting in a minimized DFA with the smallest number of states that recognizes the same language.
 *
 * Main steps:
 *   1. Initialization: Mark pairs of states as distinguishable if one is final and the other is not.
 *   2. Iterative marking: Mark pairs as distinguishable if their transitions lead to distinguishable states,
 *      or if only one state has a transition for a given symbol.
 *   3. Partitioning: Group equivalent states and build the minimized DFA.
 *
 * Helper methods are provided for partitioning, union-find operations, and pair representation.
 */
package com.compiler.lexer;

import java.util.*;

import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;


/**
 * Implements DFA minimization using the table-filling algorithm.
 */
/**
 * Utility class for minimizing DFAs using the table-filling algorithm.
 */
public class DfaMinimizer {
    /**
     * Default constructor for DfaMinimizer.
     */
        public DfaMinimizer() {
            // TODO: Implement constructor if needed
        }

    /**
     * Minimizes a given DFA using the table-filling algorithm.
     *
     * @param originalDfa The original DFA to be minimized.
     * @param alphabet The set of input symbols.
     * @return A minimized DFA equivalent to the original.
     */
    public static DFA minimizeDfa(DFA originalDfa, Set<Character> alphabet) {
    // TODO: Implement minimizeDfa
    /*
     Pseudocode:
     1. Collect and sort all DFA states
     2. Initialize table of state pairs; mark pairs as distinguishable if one is final and the other is not
     3. Iteratively mark pairs as distinguishable if their transitions lead to distinguishable states or only one has a transition
     4. Partition states into equivalence classes (using union-find)
     5. Create new minimized states for each partition
     6. Reconstruct transitions for minimized states
     7. Set start state and return minimized DFA
    */


    // ===== PREPROCESADO: añadir estado trampa =====
        // Primero recogemos todos los estados y vemos si faltan transiciones.
        List<DfaState> allStates = new ArrayList<>(originalDfa.allStates);
        boolean needsTrap = false;
        for (DfaState s : allStates) {
            for (Character c : alphabet) {
                if (s.getTransition(c) == null) {
                    needsTrap = true;
                    break;
                }
            }
            if (needsTrap) break;
        }

        // Si falta alguna transición, creamos un estado trampa no final.
        DfaState trap = null;
        if (needsTrap) {
            trap = new DfaState(Collections.emptySet()); // set vacío de estados NFA
            trap.isFinal = false;
            // el trampa se apunta a sí mismo en todas las letras
            for (Character c : alphabet) {
                trap.addTransition(c, trap);
            }
            // para cada estado existente, añadimos transiciones faltantes hacia el trampa
            for (DfaState s : allStates) {
                for (Character c : alphabet) {
                    if (s.getTransition(c) == null) {
                        s.addTransition(c, trap);
                    }
                }
            }
            allStates.add(trap);
        }

        // ===== A partir de aquí comienza el algoritmo estándar =====

        // 1. Lista de estados para indexación
        List<DfaState> states = new ArrayList<>(allStates);
        int n = states.size();

        // 2. Inicializar tabla: pares distinguibles si exactamente uno es final
        Map<Pair, Boolean> distinguishable = new HashMap<>();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                DfaState s1 = states.get(i), s2 = states.get(j);
                boolean isDist = s1.isFinal != s2.isFinal;
                distinguishable.put(new Pair(s1, s2), isDist);
            }
        }

        // 3. Iterar marcando pares distinguibles
        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < n; i++) {
                for (int j = i + 1; j < n; j++) {
                    Pair p = new Pair(states.get(i), states.get(j));
                    if (distinguishable.get(p) == Boolean.TRUE) continue;
                    for (Character c : alphabet) {
                        DfaState t1 = states.get(i).getTransition(c);
                        DfaState t2 = states.get(j).getTransition(c);
                        // transiciones siempre existen gracias al estado trampa
                        if (t1 == null || t2 == null) continue; 
                        Pair tp = new Pair(t1, t2);
                        if (distinguishable.get(tp) == Boolean.TRUE) {
                            distinguishable.put(p, true);
                            changed = true;
                            break;
                        }
                    }
                }
            }
        } while (changed);

        // 4. Particionar estados no distinguidos mediante union‐find
        List<Set<DfaState>> partitions = createPartitions(states, distinguishable);

        // 5. Crear nuevos estados de la DFA minimizada
        Map<Set<DfaState>, DfaState> newStateMap = new HashMap<>();
        for (Set<DfaState> part : partitions) {
            // unir todos los NFA states (para nombrar) y marcar final si cualquiera lo era
            Set<com.compiler.lexer.nfa.State> union = new HashSet<>();
            boolean isFinal = false;
            for (DfaState d : part) {
                union.addAll(d.getName());
                isFinal |= d.isFinal;
            }
            DfaState ns = new DfaState(union);
            ns.isFinal = isFinal;
            newStateMap.put(part, ns);
        }

        // 6. Reconstruir transiciones
        for (Map.Entry<Set<DfaState>, DfaState> entry : newStateMap.entrySet()) {
            // escoger un representante para las transiciones
            DfaState rep = entry.getKey().iterator().next();
            for (Character c : alphabet) {
                DfaState target = rep.getTransition(c);
                // encontrar la partición a la que pertenece el estado destino
                for (Set<DfaState> part : partitions) {
                    if (part.contains(target)) {
                        entry.getValue().addTransition(c, newStateMap.get(part));
                        break;
                    }
                }
            }
        }

        // 7. Determinar el estado inicial minimizado
        DfaState newStart = null;
        for (Set<DfaState> part : partitions) {
            if (part.contains(originalDfa.startState)) {
                newStart = newStateMap.get(part);
                break;
            }
        }

        return new DFA(newStart, new ArrayList<>(newStateMap.values()));
    }

    /**
     * Groups equivalent states into partitions using union-find.
     *
     * @param allStates List of all DFA states.
     * @param table Table indicating which pairs are distinguishable.
     * @return List of partitions, each containing equivalent states.
     */
    private static List<Set<DfaState>> createPartitions(List<DfaState> allStates, Map<Pair, Boolean> table) {
    // TODO: Implement createPartitions
    /*
     Pseudocode:
     1. Initialize each state as its own parent
     2. For each pair not marked as distinguishable, union the states
     3. Group states by their root parent
     4. Return list of partitions
    */


        Map<DfaState,DfaState> parent = new HashMap<>();
        for (DfaState s : allStates) parent.put(s, s);
        for (int i = 0; i < allStates.size(); i++) {
            for (int j = i + 1; j < allStates.size(); j++) {
                DfaState s1 = allStates.get(i), s2 = allStates.get(j);
                Pair p = new Pair(s1,s2);
                if (table.get(p) != Boolean.TRUE) {
                    union(parent, s1, s2);
                }
            }
        }
        Map<DfaState,Set<DfaState>> groups = new HashMap<>();
        for (DfaState s : allStates) {
            DfaState root = find(parent, s);
            groups.computeIfAbsent(root, k -> new HashSet<>()).add(s);
        }
        return new ArrayList<>(groups.values());
    
    
    }//FIN createPartitions

    /**
     * Finds the root parent of a state in the union-find structure.
     * Implements path compression for efficiency.
     *
     * @param parent Parent map.
     * @param state State to find.
     * @return Root parent of the state.
     */
    private static DfaState find(Map<DfaState, DfaState> parent, DfaState state) {
    // TODO: Implement find
    /*
     Pseudocode:
     If parent[state] == state, return state
     Else, recursively find parent and apply path compression
     Return parent[state]
    */
    
        DfaState p = parent.get(state);
        if (p != state) {
            parent.put(state, find(parent, p));
        }
        return parent.get(state);
   
   
    }//FIN find

    /**
     * Unites two states in the union-find structure.
     *
     * @param parent Parent map.
     * @param s1 First state.
     * @param s2 Second state.
     */
    private static void union(Map<DfaState, DfaState> parent, DfaState s1, DfaState s2) {
    // TODO: Implement union
    /*
     Pseudocode:
     Find roots of s1 and s2
     If roots are different, set parent of one to the other
    */
    

        DfaState ra = find(parent,s1);
        DfaState rb = find(parent,s2);
        if (ra != rb) parent.put(ra, rb);

    }

    /**
     * Helper class to represent a pair of DFA states in canonical order.
     * Used for table indexing and comparison.
     */
    private static class Pair {
        final DfaState s1;
        final DfaState s2;

        /**
         * Constructs a pair in canonical order (lowest id first).
         * @param s1 First state.
         * @param s2 Second state.
         */
        public Pair(DfaState s1, DfaState s2) {
            // TODO: Implement Pair constructor
            /*
             Pseudocode:
             Assign s1 and s2 so that s1.id <= s2.id
            */
            // order by id to ensure canonical representation
            if (s1.id <= s2.id) {
                this.s1 = s1;
                this.s2 = s2;
            } else {
                this.s1 = s2;
                this.s2 = s1;
            }
        }

        @Override
        public boolean equals(Object o) {
            // TODO: Implement equals
            /*
             Pseudocode:
             Return true if both s1 and s2 ids match
            */
            if (this == o) return true;
            if (!(o instanceof Pair)) return false;
            Pair other = (Pair) o;
            return s1 == other.s1 && s2 == other.s2;
        }

        @Override
        public int hashCode() {
            // TODO: Implement hashCode
            /*
             Pseudocode:
             Return hash of s1.id and s2.id
            */
            return Objects.hash(s1.id, s2.id);
        }
    }
}