import java.util.*;

public class SymbolTable {
    // Stack of scopes — each scope is a map of variable name → type
    private final Deque<Map<String, String>> scopes = new ArrayDeque<>();

    public SymbolTable() {
        pushScope(); // global scope
    }

    public void pushScope() {
        scopes.push(new HashMap<>());
    }

    public void popScope() {
        scopes.pop();
    }

    public void define(String name, String type) {
        scopes.peek().put(name, type);
    }

    /** Check if a variable is defined in ANY scope (walks up the stack). */
    public boolean isDefined(String name) {
        for (Map<String, String> scope : scopes) {
            if (scope.containsKey(name)) return true;
        }
        return false;
    }

    /** Get the type of a variable by walking up the scope stack. */
    public String getType(String name) {
        for (Map<String, String> scope : scopes) {
            if (scope.containsKey(name)) return scope.get(name);
        }
        return null;
    }

    /** Check if a variable is defined in the CURRENT (innermost) scope only. */
    public boolean isDefinedInCurrentScope(String name) {
        return scopes.peek().containsKey(name);
    }
}