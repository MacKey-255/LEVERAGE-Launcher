package leverage.exceptions;

import leverage.util.Urls;

public class CheatsDetectedException extends Exception {
    public CheatsDetectedException(String message) {
        super(message);
        // Avisar al Servidor que el Cliente contiene Parches
        sendServerWarning();

    }
    public void sendServerWarning() {
        String url = Urls.cheatsWarning;

    }
}