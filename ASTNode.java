import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {
    public int line;

    public abstract void accept(Visitor visitor);

    // visitor interafce will ve used in semantic analysis and code generating
    // for traversing over the nodes
    public interface Visitor {
        void visit(ProgramNode node);
        void visit(VarDeclNode node);
        void visit(AssignNode node);
        void visit(PrintNode node);
        void visit(IfNode node);
        void visit(WhileNode node);
        void visit(ForNode node);
    }

    public static class ProgramNode extends ASTNode {
        public List<ASTNode> statements = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class VarDeclNode extends ASTNode {
        public String name;
        public String dataType;
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class AssignNode extends ASTNode {
        public String expression;
        public String name;
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class IfNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        public List<ASTNode> elseBody = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class PrintNode extends ASTNode {
        public String expression;
        @Override
        public void accept(Visitor visitor) {
        visitor.visit(this);
        }
    }

    public static class WhileNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class ForNode extends ASTNode {
        public String loopVar;
        public String startExpr;
        public String endExpr;
        public List<ASTNode> body = new ArrayList<>();
        @Override
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }
}

/*
base class for all ast nodes
implementing the visitor design pattern to allow tree traversal
without modifying the node classes will be used in semantic analysis and code generation
*/