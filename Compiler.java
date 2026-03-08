import java.util.List;

public class Compiler {
    public static void main(String[] args) {
        String pseudoCode = 
        "DECLARE x AS INT\n" +
            "x = 5\n" +
            "WHILE x > 0 DO\n" +
            "  PRINT x\n" +
            "  x = x - 1\n" +
            "ENDWHILE\n" +
            "IF x = 0 THEN\n" + 
            "  PRINT x\n" +
            "ENDIF";

        System.out.println(" SOURCE PSEUDOCODE ");
        System.out.println(pseudoCode);

        //Lexicall Analysis
        Lexer lexer = new Lexer(pseudoCode);
        List<Token> tokens = lexer.tokenize();

        //Syntax Analysis
        Parser parser = new Parser(tokens);
        ASTNode.ProgramNode ast = parser.parse();
        System.out.println("\n[+] Syntax Analysis Passed");

        //Semantic Analysis
        SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
        semanticAnalyzer.analyze(ast);
        System.out.println("[+] Semantic Analysis Passed");

        //Code Generation
        CodeGenerator generator = new CodeGenerator();
        String goCode = generator.generate(ast);

        System.out.println(" GENERATED GO CODE ");
        System.out.println(goCode);
    }
}

// run karne ke liye javac *.java
//                   java Compiler