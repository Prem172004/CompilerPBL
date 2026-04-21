public class Token {
    public enum TokenType {
        // Types & Declarations
        DECLARE, AS, INT, STRING, BOOLEAN,
        // Control Flow
        IF, THEN, ELSE, ELSEIF, ENDIF,
        WHILE, DO, ENDWHILE,
        FOR, TO, ENDFOR,
        PRINT,
        // Functions
        FUNCTION, ENDFUNCTION, RETURN, RETURNS,
        // Input
        INPUT,
        // Values & Operators
        IDENTIFIER, NUMBER, STRING_LITERAL, TRUE, FALSE,
        AND, OR, NOT, OPERATOR,
        // Delimiters
        LBRACKET, RBRACKET, LPAREN, RPAREN, COMMA,
        // End of file
        EOF
    }

    public TokenType type;
    public String value;
    public int line;

    public Token(TokenType type, String value, int line) {
        this.type = type;
        this.value = value;
        this.line = line;
    }

    @Override
    public String toString() { return type + "('" + value + "') [Line: " + line + "]"; }
}