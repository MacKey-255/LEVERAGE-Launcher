package leverage.proxy.matchers;

public class JoinServerMatcher implements URLMatcher {
    private final String JOIN_URL = "http://session.minecraft.net/game/joinserver.jsp";

    @Override
    public final boolean match(String url) {
        System.out.println("Join Server: " + url);
        return url.contains(JOIN_URL) && url.split("\\?").length == 2;
    }

    @Override
    public final String handle(String url) {
        String[] segments = url.split("\\?");
        System.out.println("Join Server: " + url);
        if (url.contains(JOIN_URL) && segments.length == 2) {
            return "http://mc.krothium.com/server/joinserver?" + segments[1];
        }
        return null;
    }
}
