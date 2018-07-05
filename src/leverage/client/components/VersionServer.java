package leverage.client.components;

import leverage.game.version.VersionType;

public class VersionServer {

    private String id;
    private String versionBase;
    private long diskSpance;
    private VersionType versionType;

    // INACTIVO

    public VersionServer(String id, String versionBase, long diskSpance, VersionType versionType) {
        this.id = id;
        this.versionBase = versionBase;
        this.diskSpance = diskSpance;
        this.versionType = versionType;
    }

    public String getId() {
        return id;
    }

    public String getVersionBase() {
        return versionBase;
    }

    public long getDiskSpance() {
        return diskSpance;
    }

    public VersionType getVersionType() {
        return versionType;
    }
}
