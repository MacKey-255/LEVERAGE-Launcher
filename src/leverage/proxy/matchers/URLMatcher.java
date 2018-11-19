package leverage.proxy.matchers;

public interface URLMatcher {
    boolean match(String url);

    String handle(String url);
}
