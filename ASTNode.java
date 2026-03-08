import java.util.ArrayList;
import java.util.List;

public abstract class ASTNode {
    public int line;

    public abstract void accept(Visitor visitor);

    public interface Visitor {
        void visit(ProgramNode node);
        void visit(VarDeclNode node);
        void visit(AssignNode node);
        void visit(PrintNode node);
        void visit(IfNode node);
        void visit(WhileNode node);

    }

    public static class ProgramNode extends ASTNode {
        public List<ASTNode> statements = new ArrayList<>();
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class VarDeclNode extends ASTNode {
        public String name;
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class AssignNode extends ASTNode {
        public String expression;
        public String name;
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class IfNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }

    public static class PrintNode extends ASTNode {
        public String expression;
        public void accept(Visitor visitor) {
        visitor.visit(this);
        }
    }

    public static class WhileNode extends ASTNode {
        public String condition;
        public List<ASTNode> body = new ArrayList<>();
        public void accept(Visitor visitor) {
            visitor.visit(this);
        }
    }
}

/*
clasic visitor Pattern pe based hain ye
symentic analyzer aur code generator ko tree pe traverse karne ke liye
bina kisi node class ko modify kiye
*/