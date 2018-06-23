package leverage.client.components;

import java.io.File;

public class Version {
    private String name;
    private String url;
    private double diskSpace;
    private final File relativeJar;
    private final File relativeJSON;

    public Version(String name) {
        this.name = name;
        this.url = null;
        relativeJar = new File("versions" + File.separator + name + File.separator + name + ".jar");
        relativeJSON = new File("versions" + File.separator + name + File.separator + name + ".json");
        this.diskSpace = relativeJar.length() + relativeJSON.length();
    }

    public Version(String name, String url, double diskSpace) {
        this.name = name;
        this.url = url;
        this.diskSpace = diskSpace;
        this.relativeJar = null;
        this.relativeJSON = null;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public double getDiskSpace() {
        return diskSpace;
    }

    public File getRelativeJar() {
        return relativeJar;
    }

    public File getRelativeJSON() {
        return relativeJSON;
    }
}
