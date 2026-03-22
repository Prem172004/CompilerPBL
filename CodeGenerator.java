public class CodeGenerator implements ASTNode.Visitor {
    private final GoWriter writer = new GoWriter();

    public String generate(ASTNode.ProgramNode ast){
        writer.writeLine("package main");
        writer.writeEmptyLine();
        writer.writeLine("import \"fmt\"");
        writer.writeEmptyLine();
        writer.writeLine("func main() {");
        writer.indent();

        visit(ast);

        writer.dedent();
        writer.writeLine("}");
        return writer.getCode();
    }

    @Override
    public void visit(ASTNode.ProgramNode node) {
        for(ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
    }
    @Override
    public void visit(ASTNode.VarDeclNode node) {
        String goType = "int"; // default
        
        if (node.dataType.equals("STRING")) {
            goType = "string";
        } else if (node.dataType.equals("BOOLEAN")) {
            goType = "bool";
        }
        writer.writeLine("var " + node.name + " " + goType);
    }
    @Override
    public void visit(ASTNode.AssignNode node) {
        writer.writeLine(node.name + " = " + node.expression);
    }
    @Override
    public void visit(ASTNode.PrintNode node){
        writer.writeLine("fmt.Println(" + node.expression + ")");
    }
    @Override
    public void visit(ASTNode.IfNode node) {
        String condition = node.condition.replace("=", "==");
        writer.writeLine("if " + condition + " {");
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();
        
        if (!node.elseBody.isEmpty()) {
            writer.writeLine("} else {");
            writer.indent();
            for (ASTNode stmt : node.elseBody) stmt.accept(this);
            writer.dedent();
        }
        writer.writeLine("}");
    }
    @Override
    public void visit(ASTNode.WhileNode node) {
        writer.writeLine("for " + node.condition + " {");
        writer.indent();
        for(ASTNode stmt: node.body) {
            stmt.accept(this);
        }
        writer.dedent();
        writer.writeLine("}");
    }
    @Override
    public void visit(ASTNode.ForNode node) {
        // Translates FOR i = 0 TO 10 DO into Go's for i := 0; i <= 10; i++
        writer.writeLine("for " + node.loopVar + " := " + node.startExpr + "; " + node.loopVar + " <= " + node.endExpr + "; " + node.loopVar + "++ {");
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();
        writer.writeLine("}");
    }
    
}