package com.compiler.parser.syntax;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.compiler.parser.grammar.Grammar;
import com.compiler.parser.grammar.Production;
import com.compiler.parser.grammar.Symbol;
import com.compiler.parser.grammar.SymbolType;

/**
 * Calculates the FIRST and FOLLOW sets for a given grammar.
 * Main task of Practice 5.
 */
public class StaticAnalyzer {
    private final Grammar grammar;
    private final Map<Symbol, Set<Symbol>> firstSets;
    private final Map<Symbol, Set<Symbol>> followSets;

    public StaticAnalyzer(Grammar grammar) {
        this.grammar = grammar;
        this.firstSets = new HashMap<>();
        this.followSets = new HashMap<>();
    }

    /**
     * Calculates and returns the FIRST sets for all symbols.
     * @return A map from Symbol to its FIRST set.
     */
    public Map<Symbol, Set<Symbol>> getFirstSets() {
        // TODO: Implement the algorithm to calculate FIRST sets.
        /*
         * Pseudocode for FIRST set calculation:
         *
         * 1. For each symbol S in grammar:
         *      - If S is a terminal, FIRST(S) = {S}
         *      - If S is a non-terminal, FIRST(S) = {}
         *
         * 2. Repeat until no changes:
         *      For each production A -> X1 X2 ... Xn:
         *          - For each symbol Xi in the right-hand side:
         *              a. Add FIRST(Xi) - {ε} to FIRST(A)
         *              b. If ε is in FIRST(Xi), continue to next Xi
         *                 Otherwise, break
         *          - If ε is in FIRST(Xi) for all i, add ε to FIRST(A)
         *
         * 3. Return the map of FIRST sets for all symbols.
         */





        // Identify or construct the ε symbol.  Some grammars represent the
        // empty string as an actual terminal named "ε"; others omit it from
        // the terminal set entirely.  Locate it if present, otherwise create
        // a new Symbol instance for use in FIRST/FOLLOW calculations.
        Symbol epsilon = null;
        for (Symbol t : grammar.getTerminals()) {
            if ("ε".equals(t.name)) {
                epsilon = t;
                break;
            }
        }
        if (epsilon == null) {
            epsilon = new Symbol("ε", SymbolType.TERMINAL);
        }

        // Initialise FIRST sets:
        // - For each terminal T (except ε), FIRST(T) = {T}.
        // - For ε, FIRST(ε) = {ε}.
        // - For each non‑terminal N, FIRST(N) starts empty.
        for (Symbol t : grammar.getTerminals()) {
            Set<Symbol> set = new HashSet<>();
            if ("ε".equals(t.name)) {
                // ε derives itself
                set.add(epsilon);
            } else {
                set.add(t);
            }
            firstSets.put(t, set);
        }
        // Ensure ε has a FIRST set even if it isn’t part of the terminal list.
        if (!firstSets.containsKey(epsilon)) {
            Set<Symbol> epsSet = new HashSet<>();
            epsSet.add(epsilon);
            firstSets.put(epsilon, epsSet);
        }
        // Non‑terminals start with empty FIRST sets.
        for (Symbol nt : grammar.getNonTerminals()) {
            firstSets.putIfAbsent(nt, new HashSet<>());
        }

        boolean changed;
        // Continue propagating FIRST set entries until no new symbols are added.
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol left = p.getLeft();
                // Special case: production with empty right‑hand side implies
                // epsilon immediately.
                if (p.getRight().isEmpty()) {
                    if (firstSets.get(left).add(epsilon)) {
                        changed = true;
                    }
                    continue;
                }
                boolean allNullable = true; // track if every symbol can derive ε
                // Examine each symbol on the RHS until a non‑nullable symbol is found.
                for (Symbol sym : p.getRight()) {
                    // Skip explicit ε symbols in the RHS; they contribute nothing
                    // except that the production might still be nullable.
                    if ("ε".equals(sym.name)) {
                        // the current symbol itself is ε – continue to next symbol
                        continue;
                    }
                    // Add FIRST(sym) \ {ε} to FIRST(left)
                    Set<Symbol> symFirst = firstSets.get(sym);
                    if (symFirst != null) {
                        for (Symbol s : symFirst) {
                            if (!"ε".equals(s.name)) {
                                if (firstSets.get(left).add(s)) {
                                    changed = true;
                                }
                            }
                        }
                        // If sym’s FIRST does not contain ε, the production cannot
                        // derive ε through this symbol, so stop examining further.
                        // Determine whether FIRST(sym) contains ε by name
                        boolean containsEps = symFirst.stream().anyMatch(x -> "ε".equals(x.name));
                        if (!containsEps) {
                            allNullable = false;
                            break;
                        }
                    } else {
                        // No FIRST set computed (unexpected), treat as non‑nullable.
                        allNullable = false;
                        break;
                    }
                }
                // If every symbol on the RHS can derive ε (or RHS was empty), add ε.
                if (allNullable) {
                    if (firstSets.get(left).add(epsilon)) {
                        changed = true;
                    }
                }
            }
        } while (changed);
        return firstSets;



    }//Fin Metodo

    /**
     * Calculates and returns the FOLLOW sets for non-terminals.
     * @return A map from Symbol to its FOLLOW set.
     */
    public Map<Symbol, Set<Symbol>> getFollowSets() {
        // TODO: Implement the algorithm to calculate FOLLOW sets.
        /*
         * Pseudocode for FOLLOW set calculation:
         *
         * 1. For each non-terminal A, FOLLOW(A) = {}
         * 2. Add $ (end of input) to FOLLOW(S), where S is the start symbol
         *
         * 3. Repeat until no changes:
         *      For each production B -> X1 X2 ... Xn:
         *          For each Xi (where Xi is a non-terminal):
         *              a. For each symbol Xj after Xi (i < j <= n):
         *                  - Add FIRST(Xj) - {ε} to FOLLOW(Xi)
         *                  - If ε is in FIRST(Xj), continue to next Xj
         *                    Otherwise, break
         *              b. If ε is in FIRST(Xj) for all j > i, add FOLLOW(B) to FOLLOW(Xi)
         *
         * 4. Return the map of FOLLOW sets for all non-terminals.
         *
         * Note: This method should call getFirstSets() first to obtain FIRST sets.
         */




        // Ensure FIRST sets have been computed; required for FOLLOW computation.
        Map<Symbol, Set<Symbol>> first = getFirstSets();

        // Identify or construct the ε symbol and the end marker.
        Symbol epsilon = null;
        for (Symbol t : grammar.getTerminals()) {
            if ("ε".equals(t.name)) {
                epsilon = t;
                break;
            }
        }
        if (epsilon == null) {
            epsilon = new Symbol("ε", SymbolType.TERMINAL);
        }
        Symbol end = new Symbol("$", SymbolType.TERMINAL);

        // Initialise FOLLOW sets: empty for each non‑terminal.
        for (Symbol nt : grammar.getNonTerminals()) {
            followSets.putIfAbsent(nt, new HashSet<>());
        }
        // Add end marker to FOLLOW(start symbol).
        Symbol start = grammar.getStartSymbol();
        followSets.get(start).add(end);

        boolean changed;
        // Iterate until no new additions are made to any FOLLOW set.
        do {
            changed = false;
            for (Production p : grammar.getProductions()) {
                Symbol A = p.getLeft();
                int rhsSize = p.getRight().size();
                // Process each position in the RHS
                for (int i = 0; i < rhsSize; i++) {
                    Symbol B = p.getRight().get(i);
                    // Only non‑terminals have FOLLOW sets.
                    if (B.type == SymbolType.NON_TERMINAL) {
                        // Compute FIRST(β) for the suffix β = X_{i+1} ... X_n
                        Set<Symbol> firstBeta = new HashSet<>();
                        boolean betaNullable = true;
                        for (int j = i + 1; j < rhsSize; j++) {
                            Symbol Xj = p.getRight().get(j);
                            // Skip explicit ε symbols in the suffix.
                            if ("ε".equals(Xj.name)) {
                                continue;
                            }
                            Set<Symbol> firstXj = first.get(Xj);
                            if (firstXj != null) {
                                for (Symbol s : firstXj) {
                                    if (!"ε".equals(s.name)) {
                                        firstBeta.add(s);
                                    }
                                }
                                // Determine whether FIRST(Xj) contains ε by name.
                                boolean containsEps = firstXj.stream().anyMatch(x -> "ε".equals(x.name));
                                // If FIRST(Xj) does not contain ε, β is not nullable.
                                if (!containsEps) {
                                    betaNullable = false;
                                    break;
                                }
                            } else {
                                betaNullable = false;
                                break;
                            }
                        }
                        // Add FIRST(β) \ {ε} to FOLLOW(B).
                        for (Symbol s : firstBeta) {
                            if (followSets.get(B).add(s)) {
                                changed = true;
                            }
                        }
                        // If β can derive ε or B is the last symbol, add FOLLOW(A).
                        if (i == rhsSize - 1 || betaNullable) {
                            Set<Symbol> followA = followSets.get(A);
                            for (Symbol s : followA) {
                                if (followSets.get(B).add(s)) {
                                    changed = true;
                                }
                            }
                        }
                    }
                }
            }
        } while (changed);
        return followSets;
    }




}//Fin Metodo