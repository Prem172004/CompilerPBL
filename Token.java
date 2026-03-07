public class Token {

    public enum TokenType {
        DECLARE, AS, INT,
        IF, THEN, ENDIF,
        WHILE, DO, ENDWHILE,
        PRINT, IDENTIFIER, NUMBER,
        OPERATOR, EOF
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
    public String toString() {
        return type + "('" + value + "') [Line: " + line + "]";
    }
}
/*Iss file main token ke type aur structure ko define kia hain
  Error handle main help ho sake issi liye line variable ko add kia gaya hain
*/