package com.compiler.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
    import java.util.List;
import java.util.Map;
import java.util.Set;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;
import com.compiler.lexer.dfa.DFA;
import com.compiler.lexer.dfa.DfaState;
import com.compiler.lexer.regex.RegexParser;

/**

* The algorithm works as follows:
 *
 * 
 *   *Each rule consists of a regular expression and an associated
 *       {@link TokenType}.  Earlier rules in the list have higher
 *       priority when two patterns match the same prefix.
 *   *Each regular expression is converted to an NFA using
 *       {@link com.compiler.lexer.regex.RegexParser#parse(String)}.
 *   *A new start state is created with ε‑transitions to the start
 *       state of each rule's NFA.  The accepting states of each
 *       individual NFA are tagged with their token type and rule
 *       priority.
 *   *The combined NFA is converted to a DFA via
 *       {@link com.compiler.lexer.NfaToDfaConverter#convertNfaToDfa(NFA, Set)},
 *       then minimised via {@link DfaMinimizer#minimizeDfa(DFA, Set)}.
 *   *During tokenisation, the DFA is simulated over the input string.
 *       The longest prefix that reaches a DFA state whose NFA state set
 *       ontains a tagged acceptor determines the next token.
 * 
 *
 * This implementation uses the longest‑match rule: the lexer always
 * consumes the longest possible lexeme that matches any rule.  If two
 * rules match the same maximal substring, the rule that appears
 * earlier in the list wins.
 */
public class Tokenizer {
    /** Represents a lexical rule pairing a regex with a token type and priority. */
    private static class Rule {
        final String regex;
        final TokenType type;
        final int priority;
        Rule(String regex, TokenType type, int priority) {
            this.regex = regex;
            this.type = type;
            this.priority = priority;
        }
    }

    private final List<Rule> rules = new ArrayList<>();
    private DFA dfa;
    private final Map<DfaState, Rule> acceptMap = new HashMap<>();
    private final Set<Character> alphabet = new HashSet<>();

    /**
     * Adds a new rule to the tokenizer.  Rules added earlier have higher
     * priority during tokenisation.
     *
     * @param regex the regular expression defining the token
     * @param type  the token type to emit when this regex matches
     */
    public void addRule(String regex, TokenType type) {
        rules.add(new Rule(regex, type, rules.size()));
    }

    /**
     * Builds the combined DFA from all added rules.  Must be called after
     * adding rules and before calling {@link #tokenize(String)}.
     */
    public void compile() {
        if (rules.isEmpty()) {
            throw new IllegalStateException("No rules defined for tokenizer");
        }
        // Build NFAs for each rule
        RegexParser parser = new RegexParser();
        List<NFA> nfas = new ArrayList<>();
        for (Rule r : rules) {
            NFA nfa = parser.parse(r.regex);
            // Mark the end state as final for this rule; also record priority.
            // Use the public endState field directly because the getter in NFA
            // is currently trivial (it returns itself recursively).
            nfa.endState.isFinal = true;
            nfas.add(nfa);
        }
        // Collect alphabet characters from all patterns (heuristic)
        for (Rule r : rules) {
            for (char c : r.regex.toCharArray()) {
                switch (c) {
                    case '|':
                    case '*':
                    case '+':
                    case '?':
                    case '(':
                    case ')':
                    case '·':
                        break;
                    default:
                        alphabet.add(c);
                }
            }
        }
        // Build combined NFA: new start with epsilon to each rule's start
        State combinedStart = new State();
        State combinedEndDummy = new State();
        for (int i = 0; i < nfas.size(); i++) {
            NFA nfa = nfas.get(i);
            // epsilon from combined start to each rule's start
            combinedStart.transitions.add(new Transition(null, nfa.startState));
        }
        NFA combinedNfa = new NFA(combinedStart, combinedEndDummy);
        // Convert to DFA
        DFA rawDfa = NfaToDfaConverter.convertNfaToDfa(combinedNfa, alphabet);
        // Minimise
        dfa = DfaMinimizer.minimizeDfa(rawDfa, alphabet);
        // Map each DFA acceptor to the highest‑priority rule whose NFA state is in its set
        for (DfaState dfaState : dfa.allStates) {
            Rule matchedRule = null;
            // For each NFA state in the DFA state's name, check if it's one of our rule end states
            for (State nfaState : dfaState.getName()) {
                if (nfaState.isFinal) {
                    // Identify which rule's NFA end this corresponds to
                    for (int i = 0; i < nfas.size(); i++) {
                        NFA nfa = nfas.get(i);
                        if (nfa.endState == nfaState) {
                            Rule r = rules.get(i);
                            if (matchedRule == null || r.priority < matchedRule.priority) {
                                matchedRule = r;
                            }
                        }
                    }
                }
            }
            if (matchedRule != null) {
                acceptMap.put(dfaState, matchedRule);
                // mark DFA state as final so simulator can use isFinal
                dfaState.isFinal = true;
            }
        }
    }

    /**
     * Tokenises the provided input string using the compiled DFA.
     *
     * @param input the source text to lex
     * @return a list of tokens in the order they were recognised
     */
    public List<Token> tokenize(String input) {
        if (dfa == null) {
            throw new IllegalStateException("Tokenizer not compiled; call compile() first");
        }
        List<Token> tokens = new ArrayList<>();
        int position = 0;
        while (position < input.length()) {
            DfaState current = dfa.startState;
            Rule lastMatchRule = null;
            int lastMatchPos = -1;
            for (int i = position; i < input.length(); i++) {
                char c = input.charAt(i);
                current = current.getTransition(c);
                if (current == null) {
                    break; // dead end
                }
                if (acceptMap.containsKey(current)) {
                    lastMatchRule = acceptMap.get(current);
                    lastMatchPos = i;
                }
            }
            if (lastMatchRule == null) {
                // Unrecognised character/sequence; emit error token
                tokens.add(new Token(TokenType.ERROR, String.valueOf(input.charAt(position))));
                position++;
            } else {
                String lexeme = input.substring(position, lastMatchPos + 1);
                TokenType t = lastMatchRule.type;
                // Skip tokens of type WHITESPACE or other ignorable types
                if (t != TokenType.WHITESPACE) {
                    tokens.add(new Token(t, lexeme));
                }
                position = lastMatchPos + 1;
            }
        }
        tokens.add(new Token(TokenType.EOF, ""));
        return tokens;
    }
}
