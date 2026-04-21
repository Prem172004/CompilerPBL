
import java.util.List;
import java.util.Set;

public class Parser {

    private final List<Token> tokens;
    private int current = 0;

    // Tokens that signal the END of an expression (expression must not consume these)
    private static final Set<Token.TokenType> EXPRESSION_STOPS = Set.of(
            Token.TokenType.THEN, Token.TokenType.DO, Token.TokenType.TO,
            Token.TokenType.EOF, Token.TokenType.ENDIF, Token.TokenType.ENDWHILE,
            Token.TokenType.ENDFOR, Token.TokenType.ENDFUNCTION,
            Token.TokenType.ELSE, Token.TokenType.ELSEIF,
            Token.TokenType.DECLARE, Token.TokenType.IF, Token.TokenType.WHILE,
            Token.TokenType.FOR, Token.TokenType.PRINT, Token.TokenType.FUNCTION,
            Token.TokenType.RETURN, Token.TokenType.INPUT,
            Token.TokenType.RBRACKET, Token.TokenType.RPAREN, Token.TokenType.COMMA
    );

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
        if (peek().type == type) {
            current++;
            return true;
        }
        return false;
    }

    /**
     * Match an OPERATOR token with a specific value (e.g., "=").
     */
    private boolean matchOperator(String value) {
        if (peek().type == Token.TokenType.OPERATOR && peek().value.equals(value)) {
            current++;
            return true;
        }
        return false;
    }

    // ─────────────────── Top-Level ───────────────────
    public ASTNode.ProgramNode parse() {
        ASTNode.ProgramNode program = new ASTNode.ProgramNode();
        while (peek().type != Token.TokenType.EOF) {
            program.statements.add(parseStatement());
        }
        return program;
    }

    // ─────────────────── Statement Dispatch ───────────────────
    private ASTNode parseStatement() {
        Token startToken = peek();
        ASTNode node;

        if (match(Token.TokenType.DECLARE)) {
            node = parseDeclare(startToken);
        } else if (peek().type == Token.TokenType.IDENTIFIER) {
            node = parseIdentifierStatement(startToken);
        } else if (match(Token.TokenType.PRINT)) {
            ASTNode.PrintNode print = new ASTNode.PrintNode();
            print.expression = parseExpression();
            node = print;
        } else if (match(Token.TokenType.IF)) {
            node = parseIfStatement(startToken);
        } else if (match(Token.TokenType.WHILE)) {
            node = parseWhileStatement(startToken);
        } else if (match(Token.TokenType.FOR)) {
            node = parseForStatement(startToken);
        } else if (match(Token.TokenType.FUNCTION)) {
            node = parseFunctionDecl(startToken);
        } else if (match(Token.TokenType.RETURN)) {
            ASTNode.ReturnNode ret = new ASTNode.ReturnNode();
            ret.expression = parseExpression();
            node = ret;
        } else if (match(Token.TokenType.INPUT)) {
            ASTNode.InputNode inp = new ASTNode.InputNode();
            inp.variableName = consume().value;
            node = inp;
        } else {
            ErrorHandler.report("SYNTAX", "Unknown statement starting with: " + peek().value, startToken.line);
            return null;
        }

        node.line = startToken.line;
        return node;
    }

    // ─────────────────── DECLARE (variable or array) ───────────────────
    private ASTNode parseDeclare(Token startToken) {
        String name = consume().value;
        if (!match(Token.TokenType.AS)) {
            ErrorHandler.report("SYNTAX", "Expected 'AS'.", startToken.line);
        }

        if (match(Token.TokenType.INT) || match(Token.TokenType.STRING) || match(Token.TokenType.BOOLEAN)) {
            String dataType = tokens.get(current - 1).value;

            // Array declaration: DECLARE arr AS INT[10]
            if (peek().type == Token.TokenType.LBRACKET) {
                consume(); // [
                ASTNode.ArrayDeclNode arrDecl = new ASTNode.ArrayDeclNode();
                arrDecl.name = name;
                arrDecl.elementType = dataType;
                arrDecl.size = consume().value; // size number
                if (!match(Token.TokenType.RBRACKET)) {
                    ErrorHandler.report("SYNTAX", "Expected ']'.", startToken.line);
                }
                return arrDecl;
            }

            // Normal variable declaration
            ASTNode.VarDeclNode decl = new ASTNode.VarDeclNode();
            decl.name = name;
            decl.dataType = dataType;
            return decl;
        } else {
            ErrorHandler.report("SYNTAX", "Expected INT, STRING, or BOOLEAN.", startToken.line);
            return null;
        }
    }

    // ─────────────────── IDENTIFIER (assign or array-assign) ───────────────────
    private ASTNode parseIdentifierStatement(Token startToken) {
        String name = consume().value;

        // Array assignment: arr[index] = value
        if (peek().type == Token.TokenType.LBRACKET) {
            consume(); // [
            String indexExpr = parseExpression();
            if (!match(Token.TokenType.RBRACKET)) {
                ErrorHandler.report("SYNTAX", "Expected ']'.", startToken.line);
            }
            if (!matchOperator("=")) {
                ErrorHandler.report("SYNTAX", "Expected '='.", startToken.line);
            }
            ASTNode.ArrayAssignNode arrAssign = new ASTNode.ArrayAssignNode();
            arrAssign.name = name;
            arrAssign.indexExpr = indexExpr;
            arrAssign.valueExpr = parseExpression();
            return arrAssign;
        }

        // Normal variable assignment: x = expr
        if (!matchOperator("=")) {
            ErrorHandler.report("SYNTAX", "Expected '='.", startToken.line);
        }
        ASTNode.AssignNode assign = new ASTNode.AssignNode();
        assign.name = name;
        assign.expression = parseExpression();
        return assign;
    }

    // ─────────────────── IF / ELSE IF / ELSE ───────────────────
    private ASTNode parseIfStatement(Token startToken) {
        ASTNode.IfNode ifNode = new ASTNode.IfNode();
        ifNode.condition = parseExpression();
        if (!match(Token.TokenType.THEN)) {
            ErrorHandler.report("SYNTAX", "Expected 'THEN'.", startToken.line);
        }

        // Main IF body
        while (peek().type != Token.TokenType.ENDIF
                && peek().type != Token.TokenType.ELSE
                && peek().type != Token.TokenType.ELSEIF
                && peek().type != Token.TokenType.EOF) {
            ifNode.body.add(parseStatement());
        }

        // ELSE IF chains
        while (match(Token.TokenType.ELSEIF)) {
            ASTNode.ElseIfBranch branch = new ASTNode.ElseIfBranch();
            branch.condition = parseExpression();
            if (!match(Token.TokenType.THEN)) {
                ErrorHandler.report("SYNTAX", "Expected 'THEN' after ELSE IF condition.", peek().line);
            }
            while (peek().type != Token.TokenType.ENDIF
                    && peek().type != Token.TokenType.ELSE
                    && peek().type != Token.TokenType.ELSEIF
                    && peek().type != Token.TokenType.EOF) {
                branch.body.add(parseStatement());
            }
            ifNode.elseIfBranches.add(branch);
        }

        // Optional ELSE block
        if (match(Token.TokenType.ELSE)) {
            while (peek().type != Token.TokenType.ENDIF && peek().type != Token.TokenType.EOF) {
                ifNode.elseBody.add(parseStatement());
            }
        }

        if (!match(Token.TokenType.ENDIF)) {
            ErrorHandler.report("SYNTAX", "Expected 'ENDIF'.", startToken.line);
        }
        return ifNode;
    }

    // ─────────────────── WHILE ───────────────────
    private ASTNode parseWhileStatement(Token startToken) {
        ASTNode.WhileNode whileNode = new ASTNode.WhileNode();
        whileNode.condition = parseExpression();
        if (!match(Token.TokenType.DO)) {
            ErrorHandler.report("SYNTAX", "Expected 'DO'.", startToken.line);
        }
        while (peek().type != Token.TokenType.ENDWHILE && peek().type != Token.TokenType.EOF) {
            whileNode.body.add(parseStatement());
        }
        if (!match(Token.TokenType.ENDWHILE)) {
            ErrorHandler.report("SYNTAX", "Expected 'ENDWHILE'.", startToken.line);
        }
        return whileNode;
    }

    // ─────────────────── FOR ───────────────────
    private ASTNode parseForStatement(Token startToken) {
        ASTNode.ForNode forNode = new ASTNode.ForNode();
        forNode.loopVar = consume().value;
        matchOperator("=");
        forNode.startExpr = parseExpression();
        match(Token.TokenType.TO);
        forNode.endExpr = parseExpression();
        match(Token.TokenType.DO);

        while (peek().type != Token.TokenType.ENDFOR && peek().type != Token.TokenType.EOF) {
            forNode.body.add(parseStatement());
        }
        match(Token.TokenType.ENDFOR);
        return forNode;
    }

    // ─────────────────── FUNCTION ───────────────────
    private ASTNode parseFunctionDecl(Token startToken) {
        ASTNode.FunctionNode funcNode = new ASTNode.FunctionNode();
        funcNode.name = consume().value;

        if (!match(Token.TokenType.LPAREN)) {
            ErrorHandler.report("SYNTAX", "Expected '(' after function name.", startToken.line);
        }

        // Parse parameter list
        if (peek().type != Token.TokenType.RPAREN) {
            do {
                String paramName = consume().value;
                if (!match(Token.TokenType.AS)) {
                    ErrorHandler.report("SYNTAX", "Expected 'AS' in parameter.", startToken.line);
                }
                if (match(Token.TokenType.INT) || match(Token.TokenType.STRING) || match(Token.TokenType.BOOLEAN)) {
                    funcNode.params.add(new ASTNode.Parameter(paramName, tokens.get(current - 1).value));
                } else {
                    ErrorHandler.report("SYNTAX", "Expected type in parameter.", startToken.line);
                }
            } while (match(Token.TokenType.COMMA));
        }

        if (!match(Token.TokenType.RPAREN)) {
            ErrorHandler.report("SYNTAX", "Expected ')' after parameters.", startToken.line);
        }

        // Optional return type: RETURNS TYPE
        if (match(Token.TokenType.RETURNS)) {
            if (match(Token.TokenType.INT) || match(Token.TokenType.STRING) || match(Token.TokenType.BOOLEAN)) {
                funcNode.returnType = tokens.get(current - 1).value;
            } else {
                ErrorHandler.report("SYNTAX", "Expected return type after 'RETURNS'.", startToken.line);
            }
        }

        // Function body
        while (peek().type != Token.TokenType.ENDFUNCTION && peek().type != Token.TokenType.EOF) {
            funcNode.body.add(parseStatement());
        }
        if (!match(Token.TokenType.ENDFUNCTION)) {
            ErrorHandler.report("SYNTAX", "Expected 'ENDFUNCTION'.", startToken.line);
        }
        return funcNode;
    }

    // ─────────────────── Expression Parsing ───────────────────
    /**
     * Parse an expression. Uses line tracking + stop-token set to prevent
     * "bleeding" into the next statement.
     */
    private String parseExpression() {
        StringBuilder expr = new StringBuilder();
        int exprLine = peek().line;

        while (!EXPRESSION_STOPS.contains(peek().type) && peek().line == exprLine) {
            Token tok = peek();

            if (tok.type == Token.TokenType.IDENTIFIER) {
                String id = consume().value;

                // Array access in expression: arr[index]
                if (peek().type == Token.TokenType.LBRACKET) {
                    consume(); // [
                    String idx = parseInnerExpression();
                    if (peek().type == Token.TokenType.RBRACKET) {
                        consume();
                    }
                    expr.append(id).append("[").append(idx).append("] ");
                } // Function call in expression: func(args)
                else if (peek().type == Token.TokenType.LPAREN) {
                    consume(); // (
                    StringBuilder args = new StringBuilder();
                    boolean first = true;
                    while (peek().type != Token.TokenType.RPAREN && peek().type != Token.TokenType.EOF) {
                        if (!first) {
                            args.append(", ");
                        }
                        args.append(parseInnerExpression());
                        first = false;
                        if (peek().type == Token.TokenType.COMMA) {
                            consume();
                        }
                    }
                    if (peek().type == Token.TokenType.RPAREN) {
                        consume();
                    }
                    expr.append(id).append("(").append(args).append(") ");
                } else {
                    expr.append(id).append(" ");
                }
            } else if (tok.type == Token.TokenType.LPAREN) {
                consume(); // (
                String inner = parseInnerExpression();
                if (peek().type == Token.TokenType.RPAREN) {
                    consume();
                }
                expr.append("(").append(inner).append(") ");
            } else if (tok.type == Token.TokenType.NUMBER
                    || tok.type == Token.TokenType.STRING_LITERAL
                    || tok.type == Token.TokenType.OPERATOR
                    || tok.type == Token.TokenType.TRUE
                    || tok.type == Token.TokenType.FALSE
                    || tok.type == Token.TokenType.AND
                    || tok.type == Token.TokenType.OR
                    || tok.type == Token.TokenType.NOT) {
                String val = consume().value;
                if (val.equals("AND")) {
                    val = "&&"; 
                }else if (val.equals("OR")) {
                    val = "||"; 
                }else if (val.equals("NOT")) {
                    val = "!"; 
                }else if (val.equals("TRUE")) {
                    val = "true"; 
                }else if (val.equals("FALSE")) {
                    val = "false";
                }
                expr.append(val).append(" ");
            } else {
                break; // unknown token type for expressions — stop
            }
        }
        return expr.toString().trim();
    }

    /**
     * Parse an expression inside brackets / parens (no line-boundary check).
     * Stops at RBRACKET, RPAREN, COMMA, or EOF.
     */
    private String parseInnerExpression() {
        StringBuilder expr = new StringBuilder();

        while (peek().type != Token.TokenType.RBRACKET
                && peek().type != Token.TokenType.RPAREN
                && peek().type != Token.TokenType.COMMA
                && peek().type != Token.TokenType.EOF) {

            Token tok = peek();

            if (tok.type == Token.TokenType.IDENTIFIER) {
                String id = consume().value;
                if (peek().type == Token.TokenType.LBRACKET) {
                    consume();
                    String idx = parseInnerExpression();
                    if (peek().type == Token.TokenType.RBRACKET) {
                        consume();
                    }
                    expr.append(id).append("[").append(idx).append("] ");
                } else if (peek().type == Token.TokenType.LPAREN) {
                    consume();
                    StringBuilder args = new StringBuilder();
                    boolean first = true;
                    while (peek().type != Token.TokenType.RPAREN && peek().type != Token.TokenType.EOF) {
                        if (!first) {
                            args.append(", ");
                        }
                        args.append(parseInnerExpression());
                        first = false;
                        if (peek().type == Token.TokenType.COMMA) {
                            consume();
                        }
                    }
                    if (peek().type == Token.TokenType.RPAREN) {
                        consume();
                    }
                    expr.append(id).append("(").append(args).append(") ");
                } else {
                    expr.append(id).append(" ");
                }
            } else if (tok.type == Token.TokenType.LPAREN) {
                consume();
                String inner = parseInnerExpression();
                if (peek().type == Token.TokenType.RPAREN) {
                    consume();
                }
                expr.append("(").append(inner).append(") ");
            } else if (tok.type == Token.TokenType.NUMBER
                    || tok.type == Token.TokenType.STRING_LITERAL
                    || tok.type == Token.TokenType.OPERATOR
                    || tok.type == Token.TokenType.TRUE
                    || tok.type == Token.TokenType.FALSE
                    || tok.type == Token.TokenType.AND
                    || tok.type == Token.TokenType.OR
                    || tok.type == Token.TokenType.NOT) {
                String val = consume().value;
                if (val.equals("AND")) {
                    val = "&&"; 
                }else if (val.equals("OR")) {
                    val = "||"; 
                }else if (val.equals("NOT")) {
                    val = "!"; 
                }else if (val.equals("TRUE")) {
                    val = "true"; 
                }else if (val.equals("FALSE")) {
                    val = "false";
                }
                expr.append(val).append(" ");
            } else {
                break;
            }
        }
        return expr.toString().trim();
    }
}
