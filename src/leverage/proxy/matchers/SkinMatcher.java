package leverage.proxy.matchers;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SkinMatcher implements URLMatcher {
    private final Pattern skinRegex = Pattern.compile("http://skins.minecraft.net/MinecraftSkins/(.+)\\.png");

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
            return "http://skins.minecraft.net/MinecraftSkins/skins/" + name + ".png";
        }
        return null;
    }
}
