public class GoWriter {
    private final StringBuilder code = new StringBuilder();
    private int indentLevel = 0;

    public void indent() {
        indentLevel++;
    }
    public void dedent() {
        indentLevel--;
    }

    public void writeLine(String text) {
        code.append("\t".repeat(Math.max(0, indentLevel)));
        code.append(text).append("\n");
    }

    public void writeEmptyLine() {
        code.append("\n");
    }

    public String getCode() {
        return code.toString();
    }
}