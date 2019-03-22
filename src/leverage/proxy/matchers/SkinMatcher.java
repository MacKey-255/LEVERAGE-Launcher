package leverage.proxy.matchers;

import leverage.util.Urls;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkinMatcher implements URLMatcher {
    private final Pattern skinRegex = Pattern.compile(Urls.skinsPath+"/"+"(.+)\\.png");

    @Override
    public final boolean match(String url) {
        Matcher m = skinRegex.matcher(url);
        return m.matches();
    }

    @Override
    public final String handle(String url) {
        Matcher m = skinRegex.matcher(url);
        if (m.matches()) {
            String name = m.group(1);
            System.out.println(Urls.skinsPath + "/" + name + ".png");
            return Urls.skinsPath + "/" + name + ".png";
        }
        return null;
    }
}
