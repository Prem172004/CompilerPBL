import java.util.ArrayList;
import java.util.List;

public class CodeGenerator implements ASTNode.Visitor {
    private final GoWriter writer = new GoWriter();

    public String generate(ASTNode.ProgramNode ast) {
        // Separate functions from main-level statements
        List<ASTNode.FunctionNode> functions = new ArrayList<>();
        List<ASTNode> mainStatements = new ArrayList<>();

        for (ASTNode stmt : ast.statements) {
            if (stmt instanceof ASTNode.FunctionNode) {
                functions.add((ASTNode.FunctionNode) stmt);
            } else {
                mainStatements.add(stmt);
            }
        }

        // Header
        writer.writeLine("package main");
        writer.writeEmptyLine();
        writer.writeLine("import \"fmt\"");
        writer.writeEmptyLine();

        // Emit top-level functions (outside main)
        for (ASTNode.FunctionNode func : functions) {
            visit(func);
            writer.writeEmptyLine();
        }

        // Emit func main()
        writer.writeLine("func main() {");
        writer.indent();
        for (ASTNode stmt : mainStatements) {
            stmt.accept(this);
        }
        writer.dedent();
        writer.writeLine("}");

        return writer.getCode();
    }

    // ─────────────────── Visitor Methods ───────────────────

    @Override
    public void visit(ASTNode.ProgramNode node) {
        for (ASTNode stmt : node.statements) {
            stmt.accept(this);
        }
    }

    @Override
    public void visit(ASTNode.VarDeclNode node) {
        writer.writeLine("var " + node.name + " " + goType(node.dataType));
    }

    @Override
    public void visit(ASTNode.AssignNode node) {
        writer.writeLine(node.name + " = " + node.expression);
    }

    @Override
    public void visit(ASTNode.PrintNode node) {
        writer.writeLine("fmt.Println(" + node.expression + ")");
    }

    @Override
    public void visit(ASTNode.IfNode node) {
        String condition = fixConditionEquals(node.condition);
        writer.writeLine("if " + condition + " {");
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();

        // ELSE IF branches
        for (ASTNode.ElseIfBranch branch : node.elseIfBranches) {
            String branchCond = fixConditionEquals(branch.condition);
            writer.writeLine("} else if " + branchCond + " {");
            writer.indent();
            for (ASTNode stmt : branch.body) stmt.accept(this);
            writer.dedent();
        }

        // ELSE branch
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
        String condition = fixConditionEquals(node.condition);
        writer.writeLine("for " + condition + " {");
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();
        writer.writeLine("}");
    }

    @Override
    public void visit(ASTNode.ForNode node) {
        writer.writeLine("for " + node.loopVar + " := " + node.startExpr
            + "; " + node.loopVar + " <= " + node.endExpr
            + "; " + node.loopVar + "++ {");
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();
        writer.writeLine("}");
    }

    @Override
    public void visit(ASTNode.FunctionNode node) {
        StringBuilder sig = new StringBuilder("func " + node.name + "(");
        for (int i = 0; i < node.params.size(); i++) {
            ASTNode.Parameter p = node.params.get(i);
            if (i > 0) sig.append(", ");
            sig.append(p.name).append(" ").append(goType(p.type));
        }
        sig.append(")");
        if (node.returnType != null) {
            sig.append(" ").append(goType(node.returnType));
        }
        sig.append(" {");
        writer.writeLine(sig.toString());
        writer.indent();
        for (ASTNode stmt : node.body) stmt.accept(this);
        writer.dedent();
        writer.writeLine("}");
    }

    @Override
    public void visit(ASTNode.ReturnNode node) {
        writer.writeLine("return " + node.expression);
    }

    @Override
    public void visit(ASTNode.InputNode node) {
        writer.writeLine("fmt.Scanln(&" + node.variableName + ")");
    }

    @Override
    public void visit(ASTNode.ArrayDeclNode node) {
        // Go fixed-size array: var arr [10]int
        writer.writeLine("var " + node.name + " [" + node.size + "]" + goType(node.elementType));
    }

    @Override
    public void visit(ASTNode.ArrayAssignNode node) {
        writer.writeLine(node.name + "[" + node.indexExpr + "] = " + node.valueExpr);
    }

    // ─────────────────── Helpers ───────────────────

    /** Convert pseudocode type name to Go type. */
    private String goType(String pseudoType) {
        if (pseudoType == null) return "";
        switch (pseudoType.toUpperCase()) {
            case "STRING":  return "string";
            case "BOOLEAN": return "bool";
            case "INT":
            default:        return "int";
        }
    }

    /**
     * Replace standalone '=' with '==' for conditions,
     * without breaking !=, <=, >=, ==.
     */
    private String fixConditionEquals(String condition) {
        return condition.replaceAll("(?<![<>!=])=(?!=)", "==");
    }
}