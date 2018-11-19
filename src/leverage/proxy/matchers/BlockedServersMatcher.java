package leverage.proxy.matchers;

public class BlockedServersMatcher implements URLMatcher {
    private final String blockURL = "https://sessionserver.mojang.com/blockedservers";

    @Override
    public final boolean match(String url) {
        return url.equalsIgnoreCase(blockURL);
    }

    @Override
    public final String handle(String url) {
        if (url.equalsIgnoreCase(blockURL)) {
            return "https://mc.krothium.com/server/blockedservers";
        }
        return null;
    }
}
