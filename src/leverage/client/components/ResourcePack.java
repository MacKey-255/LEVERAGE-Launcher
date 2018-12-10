package leverage.client.components;

import java.io.File;

public class ResourcePack {
    private String name;
    private String url;
    private String description;
    private long diskSpace;
    private final File relativeZip;

    // INACTIVO

    public ResourcePack(File file, String description) {
        this.name = file.getName().replace(".zip", "");;
        this.url = null;
        this.description = description;
        this.relativeZip = file;;
        this.diskSpace = relativeZip.length();
    }

    public ResourcePack(String name, String url, String description, long diskSpace) {
        this.name = name;
        this.url = url;
        this.description = null;
        this.diskSpace = diskSpace;
        this.relativeZip = null;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getDescription() {
        return description;
    }

    public long getDiskSpace() {
        return diskSpace;
    }

    public File getRelativeZip() {
        return relativeZip;
    }
}
