package leverage.client.components;

import java.io.File;

public class Mod {

    private String id;
    private String name;
    private String url;
    private String nameJar;
    private String version;
    private String vmc;
    private long diskSpace;
    private final File relativeJar;

    public Mod(String id, String name, String url, String nameJar, String version, String vmc) {
        this.id = id;
        this.name = name;
        this.url = url;
        this.nameJar = nameJar;
        this.version = version;
        this.vmc = vmc;
        relativeJar = new File(url);
        this.diskSpace = relativeJar.length();
    }

    public Mod(String id, String name, String url, String version, long diskSpace, String vmc) {
        this.name = name;
        this.url = url;
        this.nameJar = null;
        this.version = version;
        this.vmc = vmc;
        this.diskSpace = diskSpace;
        relativeJar = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public String getVersion() {
        return version;
    }

    public long getdiskSpace() {
        return diskSpace;
    }

    public String getNameJar() {
        return nameJar;
    }

    public File getRelativeJar() {
        return relativeJar;
    }

    public String gerVMC() {
        return vmc;
    }
}
