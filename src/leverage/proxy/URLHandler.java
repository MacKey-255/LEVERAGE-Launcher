package leverage.proxy;

import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

public class URLHandler implements URLStreamHandlerFactory {
    private final HttpsHandler HTTPS_HANDLER = new HttpsHandler();
    private final HttpHandler HTTP_HANDLER = new HttpHandler();

    @Override
    public final URLStreamHandler createURLStreamHandler(String protocol) {
        if ("https".equalsIgnoreCase(protocol)) {
            return HTTPS_HANDLER;
        }
        if ("http".equalsIgnoreCase(protocol)) {
            return HTTP_HANDLER;
        }
        return null;
    }

}
