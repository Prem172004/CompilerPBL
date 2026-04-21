import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Lexer {
    private final String input;

    private static final Map<String, Token.TokenType> KEYWORDS = new HashMap<>();
    static {
        KEYWORDS.put("DECLARE", Token.TokenType.DECLARE);
        KEYWORDS.put("AS", Token.TokenType.AS);
        KEYWORDS.put("INT", Token.TokenType.INT);
        KEYWORDS.put("IF", Token.TokenType.IF);
        KEYWORDS.put("ELSE", Token.TokenType.ELSE);
        KEYWORDS.put("THEN", Token.TokenType.THEN);
        KEYWORDS.put("ENDIF", Token.TokenType.ENDIF);
        KEYWORDS.put("WHILE", Token.TokenType.WHILE);
        KEYWORDS.put("DO", Token.TokenType.DO);
        KEYWORDS.put("ENDWHILE", Token.TokenType.ENDWHILE);
        KEYWORDS.put("PRINT", Token.TokenType.PRINT);
        KEYWORDS.put("STRING", Token.TokenType.STRING);
        KEYWORDS.put("BOOLEAN", Token.TokenType.BOOLEAN);
        KEYWORDS.put("FOR", Token.TokenType.FOR);
        KEYWORDS.put("ENDFOR", Token.TokenType.ENDFOR);
        KEYWORDS.put("AND", Token.TokenType.AND);
        KEYWORDS.put("OR", Token.TokenType.OR);
        KEYWORDS.put("NOT", Token.TokenType.NOT);
        KEYWORDS.put("TO", Token.TokenType.TO);
        KEYWORDS.put("TRUE", Token.TokenType.TRUE);
        KEYWORDS.put("FALSE", Token.TokenType.FALSE);
        KEYWORDS.put("FUNCTION", Token.TokenType.FUNCTION);
        KEYWORDS.put("ENDFUNCTION", Token.TokenType.ENDFUNCTION);
        KEYWORDS.put("RETURN", Token.TokenType.RETURN);
        KEYWORDS.put("RETURNS", Token.TokenType.RETURNS);
        KEYWORDS.put("INPUT", Token.TokenType.INPUT);
    }

    public Lexer(String input) {
        this.input = input;
    }

    public List<Token> tokenize() {
        List<Token> tokens = new ArrayList<>();
        String[] lines = input.split("\\r?\\n");

        // Updated regex: multi-char ops BEFORE single-char ops; added brackets, parens,
        // comma
        Pattern pattern = Pattern.compile(
                "\"([^\"]*)\"" + // string literal
                        "|(?<word>[a-zA-Z_]\\w*)" + // word (identifier or keyword)
                        "|(?<num>\\d+)" + // number
                        "|(?<multiop>!=|<=|>=|==)" + // multi-char operators
                        "|(?<op>[=+\\-*/<>%])" + // single-char operators
                        "|(?<lbracket>\\[)" + // [
                        "|(?<rbracket>\\])" + // ]
                        "|(?<lparen>\\()" + // (
                        "|(?<rparen>\\))" + // )
                        "|(?<comma>,)" // ,
        );

        for (int i = 0; i < lines.length; i++) {
            int lineNumber = i + 1;
            Matcher matcher = pattern.matcher(lines[i]);

            while (matcher.find()) {
                if (matcher.group().trim().isEmpty())
                    continue;

                if (matcher.group().startsWith("\"")) {
                    tokens.add(new Token(Token.TokenType.STRING_LITERAL, matcher.group(), lineNumber));
                } else if (matcher.group("word") != null) {
                    String word = matcher.group("word");
                    // Case-insensitive: look up the UPPERCASE version
                    String upper = word.toUpperCase();
                    Token.TokenType type = KEYWORDS.getOrDefault(upper, Token.TokenType.IDENTIFIER);
                    // Keywords get their uppercase canonical form; identifiers keep original casing
                    String tokenValue = (type == Token.TokenType.IDENTIFIER) ? word : upper;
                    tokens.add(new Token(type, tokenValue, lineNumber));
                } else if (matcher.group("num") != null) {
                    tokens.add(new Token(Token.TokenType.NUMBER, matcher.group("num"), lineNumber));
                } else if (matcher.group("multiop") != null) {
                    tokens.add(new Token(Token.TokenType.OPERATOR, matcher.group("multiop"), lineNumber));
                } else if (matcher.group("op") != null) {
                    tokens.add(new Token(Token.TokenType.OPERATOR, matcher.group("op"), lineNumber));
                } else if (matcher.group("lbracket") != null) {
                    tokens.add(new Token(Token.TokenType.LBRACKET, "[", lineNumber));
                } else if (matcher.group("rbracket") != null) {
                    tokens.add(new Token(Token.TokenType.RBRACKET, "]", lineNumber));
                } else if (matcher.group("lparen") != null) {
                    tokens.add(new Token(Token.TokenType.LPAREN, "(", lineNumber));
                } else if (matcher.group("rparen") != null) {
                    tokens.add(new Token(Token.TokenType.RPAREN, ")", lineNumber));
                } else if (matcher.group("comma") != null) {
                    tokens.add(new Token(Token.TokenType.COMMA, ",", lineNumber));
                } else {
                    ErrorHandler.report("LEXICAL", "Unexpected character: " + matcher.group(), lineNumber);
                }
            }
        }

        // Post-processing: merge adjacent ELSE + IF into a single ELSEIF token
        tokens = mergeElseIf(tokens);

        tokens.add(new Token(Token.TokenType.EOF, "", lines.length));
        return tokens;
    }

    /**
     * When an ELSE token is immediately followed by an IF token,
     * merge them into a single ELSEIF virtual token.
     */
    private List<Token> mergeElseIf(List<Token> tokens) {
        List<Token> result = new ArrayList<>();
        for (int i = 0; i < tokens.size(); i++) {
            if (tokens.get(i).type == Token.TokenType.ELSE
                    && i + 1 < tokens.size()
                    && tokens.get(i + 1).type == Token.TokenType.IF) {
                result.add(new Token(Token.TokenType.ELSEIF, "ELSEIF", tokens.get(i).line));
                i++; // skip the IF token
            } else {
                result.add(tokens.get(i));
            }
        }
        return result;
    }
}