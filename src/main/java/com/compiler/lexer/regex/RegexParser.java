package com.compiler.lexer.regex;

import java.util.Stack;

import com.compiler.lexer.nfa.NFA;
import com.compiler.lexer.nfa.State;
import com.compiler.lexer.nfa.Transition;


/**
 * RegexParser
 * -----------
 * This class provides functionality to convert infix regular expressions into nondeterministic finite automata (NFA)
 * using Thompson's construction algorithm. It supports standard regex operators: concatenation (·), union (|),
 * Kleene star (*), optional (?), and plus (+). The conversion process uses the Shunting Yard algorithm to transform
 * infix regex into postfix notation, then builds the corresponding NFA.
 *
 * Features:
 * - Parses infix regular expressions and converts them to NFA.
 * - Supports regex operators: concatenation, union, Kleene star, optional, plus.
 * - Implements Thompson's construction rules for NFA generation.
 *
 * Example usage:
 * <pre>
 *     RegexParser parser = new RegexParser();
 *     NFA nfa = parser.parse("a(b|c)*");
 * </pre>
 */
/**
 * Parses regular expressions and constructs NFAs using Thompson's construction.
 */
public class RegexParser {
    /**
     * Default constructor for RegexParser.
     */
        public RegexParser() {
            // TODO: Implement constructor if needed
        }

    /**
     * Converts an infix regular expression to an NFA.
     *
     * @param infixRegex The regular expression in infix notation.
     * @return The constructed NFA.
     */
    public NFA parse(String infixRegex) {
    // TODO: Implement parse
    // Pseudocode: Convert infix to postfix, then build NFA from postfix
        if (infixRegex == null || infixRegex.isEmpty()) {
            throw new IllegalArgumentException("Regex cannpt be null or Empty");
        }


        //String withConcat = ShuntingYard.insertConcatenationOperator(infixRegex);
    
        
        String postfix = ShuntingYard.toPostfix(infixRegex);
        NFA nfa = buildNfaFromPostfix(postfix);
        // Marcar el estado final como estado de aceptación
        nfa.endState.isFinal = true;
        return nfa;

    }

    /**
     * Builds an NFA from a postfix regular expression.
     *
     * @param postfixRegex The regular expression in postfix notation.
     * @return The constructed NFA.
     */
    private NFA buildNfaFromPostfix(String postfixRegex) {
    // TODO: Implement buildNfaFromPostfix
    // Pseudocode: For each char in postfix, handle operators and operands using a stack

        Stack<NFA> stack = new Stack<>();

        for (char c : postfixRegex.toCharArray()) {
            if (isOperand(c)) {
                stack.push(createNfaForCharacter(c));
            } else {
                switch (c) {
                    case '.':
                    case '·': handleConcatenation(stack); break;
                    case '|': handleUnion(stack); break;
                    case '*': handleKleeneStar(stack); break;
                    case '+': handlePlus(stack); break;
                    case '?': handleOptional(stack); break;
                    default:
                        throw new IllegalArgumentException("Unknown operator: " + c);
                }
            }
        }

        if (stack.size() != 1) {
            throw new IllegalStateException("Invalid regex: stack size not 1 at end");
        }

        return stack.pop();

    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handles the '?' operator (zero or one occurrence).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or one occurrence.
     * @param stack The NFA stack.
     */
    private void handleOptional(Stack<NFA> stack) {
    // TODO: Implement handleOptional
    // Pseudocode: Pop NFA, create new start/end, add epsilon transitions for zero/one occurrence

        if (stack.isEmpty()) {
            throw new IllegalStateException("Not enough NFAs for optional operator");
        }

        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();

        start.transitions.add(new Transition(null, end)); //From new Start to new end
        start.transitions.add(new Transition(null, nfa.startState));// From new start to original sstart

        nfa.endState.isFinal = false;
        nfa.endState.transitions.add(new Transition(null, end));

        stack.push(new NFA(start, end));


    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handles the '+' operator (one or more occurrences).
     * Pops an NFA from the stack and creates a new NFA that accepts one or more occurrences.
     * @param stack The NFA stack.
     */
    private void handlePlus(Stack<NFA> stack) {
    // TODO: Implement handlePlus
    // Pseudocode: Pop NFA, create new start/end, add transitions for one or more occurrence

        if (stack.isEmpty()) {
            throw new IllegalStateException("Not enough NFAs for plus operator");
        }

        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();

            
        start.transitions.add(new Transition(null, nfa.startState));//From new start to original start(at least one occurrence)
        
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));//From origianl end back to origina;l start
        
        
        nfa.endState.transitions.add(new Transition(null, end));//from orignal end to new end
        
        
        //nfa.endState.isFinal = false;//The original end state is no longer final
        
        stack.push(new NFA(start, end));

   // throw new UnsupportedOperationException("Not implemented");
    }
    
    /**
     * Creates an NFA for a single character.
     * @param c The character to create an NFA for.
     * @return The constructed NFA.
     */
    private NFA createNfaForCharacter(char c) {
    // TODO: Implement createNfaForCharacter
    // Pseudocode: Create start/end state, add transition for character

        State start = new State();
        State end = new State();
        
       
        start.transitions.add(new Transition(c, end));//Transitiosn for the char ferom the start to end
        
        NFA nfa = new NFA(start, end);

        return nfa;


    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handles the concatenation operator (·).
     * Pops two NFAs from the stack and connects them in sequence.
     * @param stack The NFA stack.
     */
    private void handleConcatenation(Stack<NFA> stack) {
    // TODO: Implement handleConcatenation
    // Pseudocode: Pop two NFAs, connect end of first to start of second

        if (stack.size() < 2) {
            throw new IllegalStateException("Not enough NFAs for concatenation");
        }
        
        NFA nfa2 = stack.pop();
        NFA nfa1 = stack.pop();

        nfa1.endState.transitions.add(new Transition(null, nfa2.startState));//Connect end of first NFA to start a second NFA 


        //nfa1.endState.isFinal = false;

        NFA result = new NFA(nfa1.startState, nfa2.endState);
        
        stack.push(result);

    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handles the union operator (|).
     * Pops two NFAs from the stack and creates a new NFA that accepts either.
     * @param stack The NFA stack.
     */
    private void handleUnion(Stack<NFA> stack) {
    // TODO: Implement handleUnion
    // Pseudocode: Pop two NFAs, create new start/end, add epsilon transitions for union

        if (stack.size() < 2) {
        throw new IllegalStateException("Not enough NFAs for union");
    }
    
        NFA nfa2 = stack.pop();
        NFA nfa1 = stack.pop();
        
        State start = new State();
        State end = new State();
        
        // Epsilon transitions from new start to both NFAs
        start.transitions.add(new Transition(null, nfa1.startState));
        start.transitions.add(new Transition(null, nfa2.startState));
        
        // Epsilon transitions from both NFA ends to new end
        nfa1.endState.transitions.add(new Transition(null, end));
        nfa2.endState.transitions.add(new Transition(null, end));
        
        // The original end states are no longer final
        //nfa1.endState.isFinal = false;
        //nfa2.endState.isFinal = false;
        
        stack.push(new NFA(start, end));

    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Handles the Kleene star operator (*).
     * Pops an NFA from the stack and creates a new NFA that accepts zero or more repetitions.
     * @param stack The NFA stack.
     */
    private void handleKleeneStar(Stack<NFA> stack) {
    // TODO: Implement handleKleeneStar
    // Pseudocode: Pop NFA, create new start/end, add transitions for zero or more repetitions

        if (stack.isEmpty()) {
        throw new IllegalStateException("Not enough NFAs for Kleene star");
        }
    
        NFA nfa = stack.pop();
        State start = new State();
        State end = new State();
       
        start.transitions.add(new Transition(null, end));//From new start to new end (for zero occurrences)
        start.transitions.add(new Transition(null, nfa.startState));//From new start to original start
        
        nfa.endState.transitions.add(new Transition(null, nfa.startState));//From original end back to original start
        nfa.endState.transitions.add(new Transition(null, end));//From original end to new end
        
        
        //nfa.endState.isFinal = false;// The original end state is no longer final
        
        stack.push(new NFA(start, end));


    //throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Checks if a character is an operand (not an operator).
     * @param c The character to check.
     * @return True if the character is an operand, false if it is an operator.
     */
    private boolean isOperand(char c) {
    // TODO: Implement isOperand
    // Pseudocode: Return true if c is not an operator

        return (c != '|' && c != '*' && c != '?' && c != '(' && c != ')' && c != '·' && c != '+');

    //throw new UnsupportedOperationException("Not implemented");
    }
}