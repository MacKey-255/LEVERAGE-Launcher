package leverage.proxy;

import leverage.proxy.matchers.CapeMatcher;
import leverage.proxy.matchers.JoinServerMatcher;
import leverage.proxy.matchers.SkinMatcher;
import leverage.proxy.matchers.URLMatcher;
import sun.net.www.protocol.http.Handler;

import java.io.IOException;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;

class HttpHandler extends Handler{

    private final URLMatcher[] urlMatchers;

    public HttpHandler() {
        urlMatchers = new URLMatcher[]{new JoinServerMatcher()};
    }

    @Override
    protected final URLConnection openConnection(URL url) throws IOException {
        return openConnection(url, null);
    }

    @Override
    protected final URLConnection openConnection(URL url, Proxy proxy) throws IOException {
        System.out.println("URL requested: " + url);
        for (URLMatcher m : urlMatchers) {
            if (m.match(url.toString())) {
                return new ConnectionHandler(url, m);
            }
        }
        return super.openConnection(url, proxy);
    }
}
