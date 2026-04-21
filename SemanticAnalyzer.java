public class SemanticAnalyzer implements ASTNode.Visitor {
    private final SymbolTable symbolTable = new SymbolTable();
    private boolean insideFunction = false;

    public void analyze(ASTNode.ProgramNode ast) {
        visit(ast);
    }

    @Override
    public void visit(ASTNode.ProgramNode node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(ASTNode.VarDeclNode node) {
        if (symbolTable.isDefinedInCurrentScope(node.name)) {
            ErrorHandler.report("SEMANTIC", "Variable '" + node.name + "' is already declared.", node.line);
        }
        symbolTable.define(node.name, node.dataType);
    }

    @Override
    public void visit(ASTNode.AssignNode node) {
        if (!symbolTable.isDefined(node.name)) {
            ErrorHandler.report("SEMANTIC", "Cannot assign to undeclared variable '" + node.name + "'.", node.line);
        }
    }

    @Override
    public void visit(ASTNode.PrintNode node) {
        if (node.expression.startsWith("\"") && node.expression.endsWith("\"")) {
            return; // string literal — nothing to validate
        }
        String[] parts = node.expression.split(" ");
        for (String part : parts) {
            // Skip operators, numbers, keywords, and function-call syntax
            if (part.matches("[a-zA-Z_]\\w*") && !symbolTable.isDefined(part)) {
                ErrorHandler.report("SEMANTIC", "Undeclared variable '" + part + "' in PRINT statement.", node.line);
            }
        }
    }

    @Override
    public void visit(ASTNode.IfNode node) {
        for (ASTNode stmt : node.body) {
            stmt.accept(this);
        }
        for (ASTNode.ElseIfBranch branch : node.elseIfBranches) {
            for (ASTNode stmt : branch.body) {
                stmt.accept(this);
            }
        }
        for (ASTNode stmt : node.elseBody) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(ASTNode.WhileNode node) {
        for (ASTNode stmt : node.body) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(ASTNode.ForNode node) {
        symbolTable.define(node.loopVar, "INT");
        for (ASTNode stmt : node.body) stmt.accept(this);
    }

    @Override
    public void visit(ASTNode.FunctionNode node) {
        // Register the function in the current scope
        String retType = node.returnType != null ? node.returnType : "VOID";
        symbolTable.define(node.name, "FUNCTION:" + retType);

        // Push a new scope for parameter & local variables
        symbolTable.pushScope();
        insideFunction = true;

        for (ASTNode.Parameter param : node.params) {
            symbolTable.define(param.name, param.type);
        }

        for (ASTNode stmt : node.body) {
            stmt.accept(this);
        }

        insideFunction = false;
        symbolTable.popScope();
    }

    @Override
    public void visit(ASTNode.ReturnNode node) {
        if (!insideFunction) {
            ErrorHandler.report("SEMANTIC", "RETURN statement outside of a function.", node.line);
        }
    }

    @Override
    public void visit(ASTNode.InputNode node) {
        if (!symbolTable.isDefined(node.variableName)) {
            ErrorHandler.report("SEMANTIC", "Cannot INPUT to undeclared variable '" + node.variableName + "'.", node.line);
        }
    }

    @Override
    public void visit(ASTNode.ArrayDeclNode node) {
        if (symbolTable.isDefinedInCurrentScope(node.name)) {
            ErrorHandler.report("SEMANTIC", "Array '" + node.name + "' is already declared.", node.line);
        }
        symbolTable.define(node.name, node.elementType + "[]");
    }

    @Override
    public void visit(ASTNode.ArrayAssignNode node) {
        if (!symbolTable.isDefined(node.name)) {
            ErrorHandler.report("SEMANTIC", "Cannot assign to undeclared array '" + node.name + "'.", node.line);
        }
    }
}