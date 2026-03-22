public class SemanticAnalyzer implements ASTNode.Visitor {
    private final SymbolTable symbolTable = new SymbolTable();

    public void analyze(ASTNode.ProgramNode ast) {
        visit(ast);
    }

    @Override
    public void visit(ASTNode.ProgramNode node) {
        for(ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
    }
    @Override
    public void visit(ASTNode.VarDeclNode node) {
        if (symbolTable.isDefined(node.name)) {
            ErrorHandler.report("SEMANTIC", "Variable '" + node.name + "' is already declared.", node.line);
        }
        symbolTable.define(node.name, "INT");
    }

    @Override
    public void visit(ASTNode.WhileNode node) {
        for(ASTNode stmt : node.body) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(ASTNode.ForNode node) {
        //symbol table main value type temperorly save kardo ex 'i'
        symbolTable.define(node.loopVar, "INT"); 
        
        for (ASTNode stmt : node.body) stmt.accept(this);
    }

    @Override
    public void visit(ASTNode.IfNode node) {
        for (ASTNode stmt : node.body) {
            stmt.accept(this);
        }
        for (ASTNode stmt : node.elseBody) {
            stmt.accept(this);
        }
    }
    @Override
    public void visit(ASTNode.AssignNode node) {
        if(!symbolTable.isDefined(node.name)) {
            ErrorHandler.report("SEMANTIC", "Cannot assign to undeclared variable '"+node.name+"'.", node.line);
        }
    }
    @Override
    public void visit(ASTNode.PrintNode node) {
        String[] parts = node.expression.split(" ");
        for (String part : parts) {
            if (part.matches("[a-zA-Z_]\\w*") && !symbolTable.isDefined(part)) {
                ErrorHandler.report("SEMANTIC", "Undeclared variable '" + part + "' in PRINT statement.", node.line);
            }
        }
    }
}