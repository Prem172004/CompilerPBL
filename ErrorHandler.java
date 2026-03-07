public class ErrorHandler {
    public static void report(String phase, String message, int line) {
        System.err.println("[" + phase + "ERROR] Line" + line + ": " + message);
        System.exit(1); // agar error hain toh rok do prossess ko yahi pe
    }
}

/*
error kiss process main hian vo pata kaise chalega
erroh handler kaun se phase main error aaya hian (lexical,syntax, semantic) va bataiga
sath main error ka line bhi mention karega ease of finding ke liye
aur error show kar ke proccess ko rok dega
*/