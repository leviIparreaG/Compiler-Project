package com.compiler.lexer;

/**
 * A Token represents a lexeme recognised by the lexer along with its
 * classification (TokenType).
 *
 * *During lexical analysis, the input string is broken into a stream
 * of tokens. Each token records the portion of the original input
 * (the lexeme) and the type describing how that lexeme should be
 * interpreted by later phases of compilation. Additional attributes
 * such as line and column numbers or semantic values can be added
 * as fields on this class if needed.
 */
public final class Token {
    /** The type of this token. */
    private final TokenType type;
    /** The actual substring from the source that formed this token. */
    private final String lexeme;

    /**
     * Constructs a new Token with the given type and lexeme.
     *
     * @param type the classification of the token
     * @param lexeme the concrete text that produced the token
     */
    public Token(TokenType type, String lexeme) {
        this.type = type;
        this.lexeme = lexeme;
    }

    /** Returns the token's type. */
    public TokenType getType() {
        return type;
    }

    /** Returns the token's lexeme. */
    public String getLexeme() {
        return lexeme;
    }

    @Override
    public String toString() {
        return type + "('" + lexeme + "')";
    }
}
