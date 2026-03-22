public class Token {
    public enum TokenType {
        // Types & Declarations
        DECLARE, AS, INT, STRING, BOOLEAN, 
        // Control Flow
        IF, THEN, ELSE, ENDIF, 
        WHILE, DO, ENDWHILE, 
        FOR, TO, ENDFOR, 
        PRINT,
        // Values & Operators
        IDENTIFIER, NUMBER, STRING_LITERAL, TRUE, FALSE, 
        AND, OR, NOT, OPERATOR, EOF
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
/*Iss file main token ke type aur structure ko define kia hain
  Error handle main help ho sake issi liye line variable ko add kia gaya hain
*/