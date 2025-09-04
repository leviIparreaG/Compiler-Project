package com.compiler.lexer.regex;

import java.util.Stack;

/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 * <p>
 * Provides methods to preprocess regular expressions by inserting explicit
 * concatenation operators, and to convert infix regular expressions to postfix
 * notation for easier parsing and NFA construction.
 */
/**
 * Utility class for regular expression parsing using the Shunting Yard
 * algorithm.
 */
public class ShuntingYard {

    /**
     * Default constructor for ShuntingYard.
     */
    public ShuntingYard() {
        // TODO: Implement constructor if needed
    }

    public static int precedence(char op) {
    switch (op) {
       // case '.':
        case '*':
        case '+':
        case '?':
            return 3;
        case '·': // concatenación
            return 2;
        case '|':
            return 1;
        default:
            return 0;
    }
}


    /**
     * Inserts the explicit concatenation operator ('·') into the regular
     * expression according to standard rules. This makes implicit
     * concatenations explicit, simplifying later parsing.
     *
     * @param regex Input regular expression (may have implicit concatenation).
     * @return Regular expression with explicit concatenation operators.
     */
    public static String insertConcatenationOperator(String regex) {
        // TODO: Implement insertConcatenationOperator
        /*
            Pseudocode:
            For each character in regex:
                - Append current character to output
                - If not at end of string:
                        - Check if current and next character form an implicit concatenation
                        - If so, append '·' to output
            Return output as string
         */

        if (regex == null || regex.isEmpty()) {
            return regex;
        }

        StringBuilder out = new StringBuilder();
        for (int i = 0; i < regex.length(); i++) {
            char c1 = regex.charAt(i);
            out.append(c1);
            if (i + 1 < regex.length()) {
                char c2 = regex.charAt(i + 1);
                boolean left = isOperand(c1) || c1 == ')' || c1 == '*' || c1 == '+' || c1 == '?';
                boolean right = isOperand(c2) || c2 == '(';
                if (left && right) out.append('·');
            }
        }
        return out.toString();

    }


    private static boolean isOperator(char c) {
        return c == '|' || c == '·' || c == '*' || c == '+' || c == '?';
    }

    /**
     * Determines if the given character is an operand (not an operator or
     * parenthesis).
     *
     * @param c Character to evaluate.
     * @return true if it is an operand, false otherwise.
     */
    private static boolean isOperand(char c) {
        // TODO: Implement isOperand
        /*
        Pseudocode:
        Return true if c is not one of: '|', '*', '?', '+', '(', ')', '·'
         */

         return (c != '|' && c != '*' && c != '?' && c != '(' && c != ')' && c != '·' && c != '+');
        //throw new UnsupportedOperationException("Not implemented");
    }



    /**
     * Converts an infix regular expression to postfix notation using the
     * Shunting Yard algorithm. This is useful for constructing NFAs from
     * regular expressions.
     *
     * @param infixRegex Regular expression in infix notation.
     * @return Regular expression in postfix notation.
     */
    public static String toPostfix(String infixRegex) {
        // TODO: Implement toPostfix
        /*
        Pseudocode:
        1. Define operator precedence map
        2. Preprocess regex to insert explicit concatenation operators
        3. For each character in regex:
            - If operand: append to output
            - If '(': push to stack
            - If ')': pop operators to output until '(' is found
            - If operator: pop operators with higher/equal precedence, then push current operator
        4. After loop, pop remaining operators to output
        5. Return output as string
         */

         // Método para obtener la precedencia de un operador

         String withConcat = insertConcatenationOperator(infixRegex);
        StringBuilder output = new StringBuilder();
        Stack<Character> ops = new Stack<>();

        for (int i = 0; i < withConcat.length(); i++) {
            char c = withConcat.charAt(i);
            if (isOperand(c)) {
                output.append(c);
            } else if (c == '(') {
                ops.push(c);
            } else if (c == ')') {
                while (!ops.isEmpty() && ops.peek() != '(') {
                    output.append(ops.pop());
                }
                if (!ops.isEmpty()) ops.pop(); // descartar '('
            } else { // operador
                if (c == '*') {
                    while (!ops.isEmpty() && precedence(ops.peek()) > precedence(c)) {
                        output.append(ops.pop());
                    }
                    ops.push(c);
                } else {
                    while (!ops.isEmpty() && ops.peek() != '(' &&
                           precedence(ops.peek()) >= precedence(c)) {
                        output.append(ops.pop());
                    }
                    ops.push(c);
                }
            }
        }
        while (!ops.isEmpty()) output.append(ops.pop());
        return output.toString();
    }//End Method
}
