import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//ye regex matcher aur pattern kya hain 

public class Lexer {
    private final String input;
    // map token ke respect main token ke values ko assign kar dega
    private static final Map<String, Token.TokenType> KEYWORDS = Map.ofEntries(
    Map.entry("DECLARE", Token.TokenType.DECLARE),
    Map.entry("AS", Token.TokenType.AS),
    Map.entry("INT", Token.TokenType.INT),
    Map.entry("IF", Token.TokenType.IF),
    Map.entry("ELSE", Token.TokenType.ELSE),
    Map.entry("THEN", Token.TokenType.THEN),
    Map.entry("ENDIF", Token.TokenType.ENDIF),
    Map.entry("WHILE", Token.TokenType.WHILE),
    Map.entry("DO", Token.TokenType.DO),
    Map.entry("ENDWHILE", Token.TokenType.ENDWHILE),
    Map.entry("PRINT", Token.TokenType.PRINT),
    Map.entry("STRING", Token.TokenType.STRING),
    Map.entry("BOOLEAN", Token.TokenType.BOOLEAN),
    Map.entry("FOR", Token.TokenType.FOR),
    Map.entry("ENDFOR", Token.TokenType.ENDFOR),
    Map.entry("AND", Token.TokenType.AND),
    Map.entry("OR", Token.TokenType.OR),
    Map.entry("NOT", Token.TokenType.NOT),
    Map.entry("TO", Token.TokenType.TO),
    Map.entry("TRUE", Token.TokenType.TRUE),
    Map.entry("FALSE", Token.TokenType.FALSE)
    );

    public Lexer(String input) {
        this.input = input;
    }
    // question jab tak yaha pe string input use nahi kia gaya upar imput main variable ko initiazile kyu karne bol raha tha 
    // because final string uninitialized nahi reh sakta hian
    
    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        String[] lines = input.split("\\r?\\n");
        Pattern pattern = Pattern.compile("\"([^\"]*)\"|(?<word>[a-zA-Z_]\\w*)|(?<num>\\d+)|(?<op>[=+\\-*/><%])");
    //  humne regex define kia hian online help leke string aur pattern matching ke liye

    for(int i = 0; i < lines.length; i++){
        int lineNumber = i+1;
        Matcher matcher = pattern.matcher(lines[i]);

        while(matcher.find()) {
            if(matcher.group().trim().isEmpty()) continue;
            // agar string empty hahin toh aage badh jao

            if (matcher.group().startsWith("\"")) {
                tokens.add(new Token(Token.TokenType.STRING_LITERAL, matcher.group(), lineNumber));
                }//chceks if matched token is a string under double quotes
            else if(matcher.group("word") != null) {
                String word = matcher.group("word");
                tokens.add(new Token(KEYWORDS.getOrDefault(word, Token.TokenType.IDENTIFIER), word, lineNumber));
            }//checks if matched token is a word(identifier or keyword)
            else if(matcher.group("num") != null) {
                tokens.add(new Token(Token.TokenType.NUMBER, matcher.group("num"), lineNumber));
            }//check if matched token is a number
            else if(matcher.group("op") != null) {
                tokens.add(new Token(Token.TokenType.OPERATOR, matcher.group("op"), lineNumber));
            }// chekc if token is a special character
            else {
                ErrorHandler.report("LEXICAL", "Unexpected character: " + matcher.group(), lineNumber);
            }// agar koi bhi nahi hain toh iska matlab ki unexprected character hain 
        }
    }
    //eof end of file token marks end of token stream and return the complete list
    tokens.add(new Token(Token.TokenType.EOF, "",lines.length));
    return tokens;
    }
}

/*
Lexer ka main function toh hum log jante he hain
converts the source code into tokens
traks the number of lines for accurate error filing
*/