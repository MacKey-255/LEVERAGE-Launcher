package leverage.client.download;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DownloadManager {

    List<String> urls;
    List<String> destine;

    public DownloadManager() {
        this.urls = new ArrayList<>();
        this.destine = new ArrayList<>();
    }

    public void addUrl(String url, String destine) {
        this.urls.add(url);
        this.destine.add(destine);
    }

    public void start() {
        //Iniciar Descarga de todos los Archivos
    }

    // Verify download URL.
    private URL verifyUrl(String url) {
        // Only allow HTTP URLs.
        if (!url.toLowerCase().startsWith("http://"))
            return null;

        // Verify format of URL.
        URL verifiedUrl = null;
        try {
            verifiedUrl = new URL(url);
        } catch (Exception e) {
            return null;
        }

        // Make sure URL specifies a file.
        if (verifiedUrl.getFile().length() < 2)
            return null;

        return verifiedUrl;
    }
}
