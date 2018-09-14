package leverage.exceptions;

public class CheatsDetectedException extends Exception {
    public CheatsDetectedException(String message) {
        super(message);
        // Avisar al Servidor que el Cliente contiene Parches

    }
}