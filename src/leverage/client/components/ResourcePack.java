package leverage.client.components;

import java.io.File;

public class ResourcePack {
    private String name;
    private String url;
    private String nameZip;
    private long diskSpace;
    private final File relativeZip;

    // INACTIVO

    public ResourcePack(String name, String nameZip) {
        this.name = name;
        this.url = null;
        this.nameZip = nameZip;
        this.relativeZip = new File("resourcepacks" + File.separator + nameZip + ".zip");;
        this.diskSpace = relativeZip.length();
    }

    public ResourcePack(String name, String url, long diskSpace) {
        this.name = name;
        this.url = url;
        this.nameZip = null;
        this.diskSpace = diskSpace;
        this.relativeZip = null;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getNameZip() {
        return nameZip;
    }

    public long getDiskSpace() {
        return diskSpace;
    }

    public File getRelativeZip() {
        return relativeZip;
    }
}
