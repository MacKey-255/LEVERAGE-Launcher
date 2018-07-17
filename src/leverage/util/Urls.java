package leverage.util;

// Clase Administradora de URLS

public class Urls {
    public static final String leverage = "http://127.0.0.1:90";        // IP Fija de la Web de LEVERAGE;
    public static final String media = leverage + "/Minecraft/web/media/";
    public static final String authPath = leverage + "/Minecraft/web/app_dev.php/user/auth";
    public static final String closeath = leverage + "/Minecraft/web/app_dev.php/user/close";
    public static final String refreshPath = leverage + "/Minecraft/web/app_dev.php/user/refresh";
    public static final String newsUrl = leverage + "/Minecraft/web/app_dev.php/news/list";
    public static final String register = leverage + "/Minecraft/web/app_dev.php/user/register";
    public static final String forgotPassword = leverage;
    public static final String whitelist = leverage + "/Minecraft/web/app_dev.php/user/white";
    public static final String blacklist = leverage + "/Minecraft/web/app_dev.php/user/black";
    public static final String skinsPath = leverage + "/Minecraft/web/app_dev.php/user/skins";
    public static final String CHANGESKIN_URL = leverage + "/Minecraft/web/app_dev.php/user/skins/upload";
    public static final String serverVersion = leverage + "/versions/version.json";
    public static final String versionManifest = leverage + "/version_manifest.json";
    public static final String cheatsWarning = leverage + "/Minecraft/web/app_dev.php/user/cheats";
    public static final String help = leverage;
    public static final String onlineData = leverage + "/Minecraft/web/app_dev.php/user/online";
    public static final String listOnline = leverage + "/Minecraft/web/app_dev.php/user/list";
    public static final String update = leverage + "/Minecraft/web/app_dev.php/user/update";

    public static final String versionList = leverage + "/mods/1.12.2/";
    public static final String modsList = leverage + "/index.php";

    // URLS para el Sistema AntiCheat
    public static final String comfirmIP = leverage;
    public static final String listData = leverage;

    // URLS Modulares

    public static final String urlDataProfileId(String id) {
        return leverage + "/Minecraft/web/app_dev.php/user/status/" + id;
    }

    public static final String skinsPathProfileId(String name) {
        return leverage + "/Minecraft/web/skins/" + name + ".png";
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
