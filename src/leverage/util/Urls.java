package leverage.util;

// Clase Administradora de URLS

public class Urls {
    public static final String leverage = "http://10.30.1.31";        // IP Fija de la Web de LEVERAGE
    public static final String media = leverage + "/media/";
    public static final String authPath = leverage + "/user/auth";
    public static final String closeath = leverage + "/user/close";
    public static final String refreshPath = leverage + "/user/refresh";
    public static final String newsUrl = leverage + "/news/list";
    public static final String register = leverage + "/user/register";
    public static final String forgotPassword = "ts3server://10.30.1.31/"; //leverage + "/user/login";
    public static final String skinsPath = leverage + "/user/skins";
    public static final String CHANGESKIN_URL = leverage + "/user/skins/upload";
    public static final String serverVersion = leverage + "/versions/version.json";
    public static final String versionManifest = leverage + "/version_manifest.json";
    public static final String cheatsWarning = leverage + "/user/cheats";
    public static final String help = leverage + "/user/donate";
    public static final String onlineData = leverage + "/user/online";
    public static final String listOnline = leverage + "/user/list";
    public static final String update = leverage + "/user/update";

    public static final String versionList = leverage + "/mods/1.12.2/";
    public static final String modsList = leverage + "/index.php";

    // Whitelist Via Web
    public static final String whitelist = leverage + "/user/white";
    public static final String blacklist = leverage + "/user/black";

    // RCON URLS
    //public static final String leverageIP = "10.30.1.31";        // IP LEVERAGE Server
    public static final int rconPort = 25595;        // Rcon Port LEVERAGE Server

    // URLS Modulares

    public static final String urlDataProfileId(String id) {
        return leverage + "/user/status/" + id;
    }

    public static final String skinsPathProfileId(String name) {
        return leverage + "/skins/" + name + ".png";
    }

    // DISABLE

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
