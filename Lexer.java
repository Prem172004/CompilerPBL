import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//ye regex matcher aur pattern kya hain 

public class Lexer {
    private final String input;
    // humne map kyu banaya hain
    private static final Map<String, Token.TokenType> KEYWORDS = Map.of(
        "DECLARE", Token.TokenType.DECLARE,
        "AS",Token.TokenType.AS,
        "INT",Token.TokenType.INT,
        "IF",Token.TokenType.IF,
        "THEN", Token.TokenType.THEN,
        "ENDIF", Token.TokenType.ENDIF,
        "WHILE",Token.TokenType.WHILE,
        "DO", Token.TokenType.DO,
        "ENDWHILE",Token.TokenType.ENDWHILE,
        "PRINT",Token.TokenType.PRINT
    );

    public Lexer(String input) {
        this.input = input;
    }
    // question jab tak yaha pe string input use nahi kia gaya upar imput main variable ko initiazile kyu karne bol raha tha 
    
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        String[] lines = input.split("\\r?\\n");
        Pattern pattern = Pattern.compile("\\s+|(?<word>[a-zA-Z_]\\w*)|(?<num>\\d+)|(?<op>[=+\\-*/><])");
    // pehele tok koi inn dono line ka matlab batao

    for(int i = 0; i < lines.length; i++){
        int lineNumber = i+1;
        Matcher matcher = pattern.matcher(lines[i]);

        while(matcher.find()) {
            if(matcher.group().trim().isEmpty()) continue;
            // explin

            if(matcher.group("word") != null) {
                String word = matcher.group("word");
                tokens.add(new Token(KEYWORDS.getOrDefault(word, Token.TokenType.IDENTIFIER), word, lineNumber));
            }
            else if(matcher.group("num") != null) {
                tokens.add(new Token(Token.TokenType.NUMBER, matcher.group("num"), lineNumber));
            }
            else if(matcher.group("op") != null) {
                tokens.add(new Token(Token.TokenType.OPERATOR, matcher.group("op"), lineNumber));
            }
            else {
                ErrorHandler.report("LEXICAL", "Unexpected character: " + matcher.group(), lineNumber);
            }
        }
    }

    tokens.add(new Token(Token.TokenType.EOF, "",lines.length));
    return tokens;
    }
}

/*
Lexer ka main function toh hum log jante he hain
converts the source code into tokens
traks the number of lines for accurate error filing
*/