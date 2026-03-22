import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

public class Compiler {
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringBuilder pseudoCode = new StringBuilder();
        String line;
        
        System.out.println("Enter pseudoCode (Type END to Finish):");
        
        // FIX: Safely read lines, handle EOF (null), ignore spaces, and ignore case
        while ((line = br.readLine()) != null) {
            if (line.trim().equalsIgnoreCase("END")) {
                break;
            }
            pseudoCode.append(line).append("\n");
        }
        
        String code = pseudoCode.toString();

        // Guard against completely empty input
        if (code.trim().isEmpty()) {
            System.out.println("No code entered. Exiting compiler.");
            return;
        }

        System.out.println("\n--- 1. SOURCE PSEUDOCODE ---");
        System.out.println(code);

        try {
            // Lexical Analysis
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.tokenize();

            // Syntax Analysis
            Parser parser = new Parser(tokens);
            ASTNode.ProgramNode ast = parser.parse();
            System.out.println("\n[+] Syntax Analysis Passed");

            // Semantic Analysis
            SemanticAnalyzer semanticAnalyzer = new SemanticAnalyzer();
            semanticAnalyzer.analyze(ast);
            System.out.println("[+] Semantic Analysis Passed");

            // Code Generation
            CodeGenerator generator = new CodeGenerator();
            String goCode = generator.generate(ast);

            System.out.println("\n--- 2. GENERATED GO CODE ---");
            System.out.println(goCode);
            
        } catch (Exception e) {
            System.err.println("\n[COMPILER FAILURE] " + e.getMessage());
        }
    }
}