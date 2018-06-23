package leverage.util;

public class Urls {
    public static final String leverage = "http://127.0.0.1";        //"mc.leverage.com";
    public static final String authPath = leverage + "/authenticate/";
    public static final String refreshPath = leverage + "/refresh/";
    public static final String newsUrl = "http://127.0.0.1/authenticate/news.json";
    public static final String register = leverage + "/register/";
    public static final String forgotPassword = leverage;

    public static final String urlDataProfileId(String id) {
        return leverage + "/users.php?profileID=" + id;
    }
}
