import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int current = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    private Token consume() {
        return tokens.get(current++);
    }
    private Token peek() {
        return tokens.get(current);
    }

    private boolean match(Token.TokenType type) {
        if(peek().type == type) {
            current++; return true;
        }
        return false;
    }

    public ASTNode.ProgramNode parse() {
        ASTNode.ProgramNode program = new ASTNode.ProgramNode();
        while(peek().type != Token.TokenType.EOF) {
            program.statements.add(parseStatement());
        }
        return program;
    }

    private ASTNode parseStatement() {
        Token startToken = peek();
        ASTNode node;

        if(match(Token.TokenType.DECLARE)) {
            ASTNode.VarDeclNode decl = new ASTNode.VarDeclNode();
            decl.name = consume().value;
            if(!match(Token.TokenType.AS) || !match(Token.TokenType.INT)) {
                ErrorHandler.report("SYNTAX","Expected 'AS INT' after variable decleration.", startToken.line);
            }
            node = decl;
        }
        else if (match(Token.TokenType.IDENTIFIER)) {
            ASTNode.AssignNode assign = new ASTNode.AssignNode();
            assign.name = tokens.get(current - 1).value;
            if (!match(Token.TokenType.OPERATOR)) ErrorHandler.report("SYNTAX", "Expected '='.", startToken.line);
            assign.expression = parseExpression();
            node = assign;
        }
        else if (match(Token.TokenType.PRINT)) {
            ASTNode.PrintNode print = new ASTNode.PrintNode();
            print.expression = parseExpression();
            node = print;
        }
        else if (match(Token.TokenType.IF)) {
            ASTNode.IfNode ifNode = new ASTNode.IfNode();
            ifNode.condition = parseExpression();
            if (!match(Token.TokenType.THEN)) ErrorHandler.report("SYNTAX", "Expected 'THEN'.", startToken.line);
            while (peek().type != Token.TokenType.ENDIF && peek().type != Token.TokenType.EOF) {
                ifNode.body.add(parseStatement());
            }
            if (!match(Token.TokenType.ENDIF)) ErrorHandler.report("SYNTAX", "Expected 'ENDIF'.", startToken.line);
            node = ifNode;
        }
        else if (match(Token.TokenType.WHILE)) {
            ASTNode.WhileNode whileNode = new ASTNode.WhileNode();
            whileNode.condition = parseExpression();
            if (!match(Token.TokenType.DO)) ErrorHandler.report("SYNTAX", "Expected 'DO'.", startToken.line);
            while (peek().type != Token.TokenType.ENDWHILE && peek().type != Token.TokenType.EOF) {
                whileNode.body.add(parseStatement());
            }
            if (!match(Token.TokenType.ENDWHILE)) ErrorHandler.report("SYNTAX", "Expected 'ENDWHILE'.", startToken.line);
            node = whileNode;
        } else {
            ErrorHandler.report("SYNTAX", "Unknown statement starting with: " + peek().value, startToken.line);
            return null; // Unreachable
        }
        
        node.line = startToken.line;
        return node;
    }

    private String parseExpression() {
        StringBuilder expr = new StringBuilder();
        while(peek().type == Token.TokenType.IDENTIFIER || peek().type == Token.TokenType.NUMBER || peek().type == Token.TokenType.OPERATOR) {
            expr.append(consume().value).append(" ");
        }
        return expr.toString().trim();
    }
}