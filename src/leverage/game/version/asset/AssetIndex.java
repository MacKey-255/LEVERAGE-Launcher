package leverage.game.version.asset;

import leverage.util.Urls;

public class AssetIndex {
    private final String id;
    private String sha1;
    private final String url;

    public AssetIndex(String id) {
        this.id = id == null ? "legacy" : id;
        url = Urls.assetsPath("indexes/" + this.id + ".json"); // "https://s3.amazonaws.com/Minecraft.Download/;
    }

    //public AssetIndex(String id, String url, String sha1) {
    public AssetIndex(String id, String sha1) {
        this.id = id == null ? "legacy" : id;
        this.url = Urls.assetsPath("indexes/" + this.id + ".json"); // "https://s3.amazonaws.com/Minecraft.Download/;
        this.sha1 = sha1;
    }

    public final String getID() {
        return id;
    }

    public final String getURL() {
        return url;
    }

    public final String getSHA1() {
        return sha1;
    }
}
