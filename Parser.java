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

        if (match(Token.TokenType.DECLARE)) {
            ASTNode.VarDeclNode decl = new ASTNode.VarDeclNode();
            decl.name = consume().value;
            match(Token.TokenType.AS);

            if (match(Token.TokenType.INT) || match(Token.TokenType.STRING) || match(Token.TokenType.BOOLEAN)) {
                decl.dataType = tokens.get(current - 1).value; 
            } else {
                ErrorHandler.report("SYNTAX", "Expected INT, STRING, or BOOLEAN", startToken.line);
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
            while (peek().type != Token.TokenType.ENDIF && peek().type != Token.TokenType.ELSE && peek().type != Token.TokenType.EOF) {
                ifNode.body.add(parseStatement());
            }
            if (match(Token.TokenType.ELSE)) {
                while (peek().type != Token.TokenType.ENDIF) {
                    ifNode.elseBody.add(parseStatement());
                }
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
        } 
        else if (match(Token.TokenType.FOR)) {
            ASTNode.ForNode forNode = new ASTNode.ForNode();
            forNode.loopVar = consume().value; // eg i
            match(Token.TokenType.OPERATOR); // '='
            forNode.startExpr = parseExpression(); // eg 0
            match(Token.TokenType.TO);
            forNode.endExpr = parseExpression(); // eg 10
            match(Token.TokenType.DO);
            
            while (peek().type != Token.TokenType.ENDFOR) {
                forNode.body.add(parseStatement());
            }
            match(Token.TokenType.ENDFOR);
            node = forNode;
        }else {
            ErrorHandler.report("SYNTAX", "Unknown statement starting with: " + peek().value, startToken.line);
            return null; // Unreachable
        }
        
        node.line = startToken.line;
        return node;
    }

    private String parseExpression() {
        StringBuilder expr = new StringBuilder();
        while (peek().type == Token.TokenType.IDENTIFIER || 
               peek().type == Token.TokenType.NUMBER || 
               peek().type == Token.TokenType.STRING_LITERAL ||
               peek().type == Token.TokenType.TRUE ||
               peek().type == Token.TokenType.FALSE ||
               peek().type == Token.TokenType.AND ||
               peek().type == Token.TokenType.OR ||
               peek().type == Token.TokenType.NOT ||
               peek().type == Token.TokenType.OPERATOR) {
            
            String val = consume().value;
            // translate logical operators for simplicity
            if (val.equals("AND")) val = "&&";
            else if (val.equals("OR")) val = "||";
            else if (val.equals("NOT")) val = "!";
            else if (val.equals("TRUE")) val = "true";
            else if (val.equals("FALSE")) val = "false";
            
            expr.append(val).append(" ");
        }
        return expr.toString().trim();
    }
}