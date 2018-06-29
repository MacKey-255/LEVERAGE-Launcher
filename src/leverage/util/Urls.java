package leverage.util;

// Clas Administradora de URLS

public class Urls {
    public static final String leverage = "http://127.0.0.1";        // IP Fija de la Web de LEVERAGE;
    public static final String authPath = leverage + "/authenticate/";
    public static final String refreshPath = leverage + "/refresh/";
    public static final String newsUrl = leverage + "/authenticate/news.json";
    public static final String register = leverage + "/register/";
    public static final String forgotPassword = leverage;
    public static final String skinsPath = leverage + "/skins/";
    public static final String CHANGESKIN_URL = leverage + "/upload.php";
    public static final String versionManifest = leverage + "/version_manifest.json";
    public static final String help = leverage + "/help";
    public static final String onlineData = leverage + "/online.json";
    public static final String listOnline = leverage + "/online/";
    public static final String update = leverage + "/update.json";

    public static final String mods = leverage + "/mods/1.12.2/";
    public static final String modsList = leverage + "/mods/1.12.2/mod_list.json";

    // URLS para el Sistema AntiCheat
    public static final String comfirmIP = leverage;
    public static final String listData = leverage;

    // URLS Modulares

    public static final String urlDataProfileId(String id) {
        return leverage + "/users.php?profileID=" + id;
    }

    public static final String listDataPath(String server) {
        return leverage + "/list/" + server + ".php";
    }

    public static final String skinsPathProfileId(String name) {
        return leverage + "/skins/" + name + ".png";
    }

    public static final String libraryPath(String library) {
        return leverage + "/libraries/" + library;
    }

    public static final String versionsPath(String version) {
        return leverage + "/versions/" + version;
    }

    public static final String modsPath(String mod) {
        return leverage + "/mods/" + mod;
    }

    public static final String assetsPath(String assets) {
        return leverage + "/assets/" + assets;
    }

    public static final String resourcePackPath(String pack) {
        return leverage + "/resources/" + pack;
    }
}
