package leverage.util;

// Clase Administradora de URLS

public class Urls {
    public static final String leverage = "http://127.0.0.1:8000";        // IP Fija de la Web de LEVERAGE
    public static final String media = leverage + "/media";
    public static final String authPath = leverage + "/api/login/";
    public static final String closeath = leverage + "/api/logout/";
    public static final String refreshPath = leverage + "/api/refresh/";
    public static final String newsUrl = leverage + "/api/news/";
    public static final String register = leverage + "/user/register/";
    public static final String forgotPassword = "/user/login/";
    public static final String skinsPath = media + "/skins/";
    public static final String CHANGESKIN_URL = leverage + "/api/skins/";
    public static final String custonSkins_URL = media + "/CustomSkinLoader.json";
    public static final String versionManifest = media + "/version_manifest.json";
    public static final String cheatsWarning = leverage + "/api/ban/";
    public static final String help = leverage + "/user/donate";
    public static final String onlineData = leverage + "/api/online/";
    public static final String listOnline = leverage + "/user/list";
    public static final String update = leverage + "/api/client/update/";
    public static final String crash = leverage + "/api/crash/";

    public static final String versionList = leverage + "/api/check/version/";
    public static final String modsList = leverage + "/api/check/mods/";
    public static final String resourceList = leverage + "/api/check/resources/";

    // Whitelist Via Web
    public static final String whitelist = leverage + "/api/white/";
    public static final String blacklist = leverage + "/api/black/";

    // RCON URLS
    //public static final String leverageIP = "10.30.1.31";        // IP LEVERAGE Server
    public static final int rconPort = 25595;        // Rcon Port LEVERAGE Server

    // URLS Modulares

    public static final String urlDataProfileId(String id) {
        return leverage + "/api/status/" + id;
    }

    public static final String skinsPathProfileId(String name) {
        return media + "/skins/" + name + ".png";
    }

    // DISABLE

    public static final String libraryPath(String library) {
        return media + "/libraries/" + library;
    }

    public static final String versionsPath(String version) {
        return media + "/versions/" + version;
    }

    public static final String assetsPath(String assets) {
        return media + "/assets/" + assets;
    }
}
