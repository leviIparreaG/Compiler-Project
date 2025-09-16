package com.compiler.lexer;

/**
 * TokenType enumerates the kinds of tokens that the lexer can emit.
 *
 * *A token is a classification for a lexeme (the concrete substring of
 * source text). In a typical compiler or interpreter, token types
 * distinguish keywords, identifiers, literals, operators, and punctuation.
 * The exact set of token types is specific to the language being lexed.
 *
 * *This enumeration defines a handful of generic token categories to
 * illustrate how a tokeniser can work. When extending the lexer for a
 * particular language, add additional entries here (for example, KEYWORD,
 * IDENTIFIER, NUMBER, STRING, OPERATOR, etc.).
 */
public enum TokenType {
    /** Represents a sequence of whitespace characters (spaces, tabs, newlines). */
    WHITESPACE,

    /** Represents an identifier (e.g., variable or function name). */
    IDENTIFIER,

    /** Represents a numeric literal (integer or floating‑point). */
    NUMBER,

    /** Represents an operator (e.g., '+', '-', '*', '/'). */
    OPERATOR,

    /** Represents any punctuation (commas, semicolons, parentheses, etc.). */
    PUNCTUATION,

    /** Represents a keyword (reserved word) in the target language. */
    KEYWORD,

    /** Represents an end‑of‑file marker; emitted when the input is fully consumed. */
    EOF,

    /** Represents an unknown or invalid token. */
    ERROR
}
