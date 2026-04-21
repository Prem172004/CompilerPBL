import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {
    public int line;

    public abstract void accept(Visitor visitor);

    // Visitor interface — extended with new node types
    public interface Visitor {
        void visit(ProgramNode node);
        void visit(VarDeclNode node);
        void visit(AssignNode node);
        void visit(PrintNode node);
        void visit(IfNode node);
        void visit(WhileNode node);
        void visit(ForNode node);
        void visit(FunctionNode node);
        void visit(ReturnNode node);
        void visit(InputNode node);
        void visit(ArrayDeclNode node);
        void visit(ArrayAssignNode node);
    }

    // ── Helper classes ──

    /** Represents a single function parameter: name AS type */
    public static class Parameter {
        public String name;
        public String type;
        public Parameter(String name, String type) {
            this.name = name;
            this.type = type;
        }
    }

    /** Represents an ELSE IF branch with its own condition and body */
    public static class ElseIfBranch {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
    }

    // ── Existing Nodes ──

    public static class ProgramNode extends ASTNode {
        public List<ASTNode> statements = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class VarDeclNode extends ASTNode {
        public String name;
        public String dataType;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class AssignNode extends ASTNode {
        public String name;
        public String expression;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class IfNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        public List<ElseIfBranch> elseIfBranches = new ArrayList<>();
        public List<ASTNode> elseBody = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class PrintNode extends ASTNode {
        public String expression;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class WhileNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class ForNode extends ASTNode {
        public String loopVar;
        public String startExpr;
        public String endExpr;
        public List<ASTNode> body = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    // ── New Nodes ──

    public static class FunctionNode extends ASTNode {
        public String name;
        public List<Parameter> params = new ArrayList<>();
        public String returnType; // null for void functions
        public List<ASTNode> body = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class ReturnNode extends ASTNode {
        public String expression;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class InputNode extends ASTNode {
        public String variableName;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class ArrayDeclNode extends ASTNode {
        public String name;
        public String elementType;
        public String size;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }

    public static class ArrayAssignNode extends ASTNode {
        public String name;
        public String indexExpr;
        public String valueExpr;
        @Override
        public void accept(Visitor visitor) { visitor.visit(this); }
    }
}