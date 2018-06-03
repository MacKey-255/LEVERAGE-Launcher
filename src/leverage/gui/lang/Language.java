package leverage.gui.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class Language {
    private static final ArrayList<String> langData =  new ArrayList<>();

    /**
     * Loads a language file
     * @param lang The language to be loaded
     * @throws IOException When data read fails
     */
    public static void loadLang(String lang) throws IOException {
        langData.clear();
        URL resource = Language.class.getResource("/leverage/gui/lang/" + lang + ".txt");
        if (resource == null) {
            resource = Language.class.getResource("/leverage/gui/lang/es-es.txt");
        }
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.openStream(), Charset.forName("UTF-8")))) {
            String line;
            while ((line = reader.readLine()) != null) {
                langData.add(line);
            }
        }
    }

    /**
     * Gets a specific localized line
     * @param line The target line
     * @return The requested localized line
     */
    public static String get(int line) {
        if (line <= langData.size()) {
            return langData.get(line - 1);
        }
        return "";
    }
}
