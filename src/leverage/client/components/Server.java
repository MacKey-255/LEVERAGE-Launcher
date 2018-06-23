package leverage.client.components;

import leverage.client.extra.StatusType;

import java.util.List;

public class Server {

    private StatusType status;
    private String name;
    private String url;
    private int players;
    private int maxPlayers;

    private List<Mod> modslist;
    private Version version;
    private Forge forge;

    public Server(StatusType status, String name, String url, int players, int maxPlayers, List<Mod> modslist, Version version, Forge forge) {
        this.status = status;
        this.name = name;
        this.url = url;
        this.players = players;
        this.maxPlayers = maxPlayers;

        this.modslist = modslist;
        this.version = version;
        this.forge = forge;
    }

    public Server(StatusType status, String name, String url, int players, int maxPlayers, Version version, Forge forge) {
        this.status = status;
        this.name = name;
        this.url = url;
        this.players = players;
        this.maxPlayers = maxPlayers;

        this.version = version;
        this.forge = forge;
    }

    public Server(String name, String url, List<Mod> modslist, Version version, Forge forge) {
        this.status = StatusType.OFFLINE;
        this.name = name;
        this.url = url;
        this.players = 0;
        this.maxPlayers = 0;

        this.modslist = modslist;
        this.version = version;
        this.forge = forge;
    }

    public StatusType getStatus() {
        return status;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }

    public int getPlayers() {
        return players;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public List<Mod> getModslist() {
        return modslist;
    }

    public Version getVersion() {
        return version;
    }

    public Forge getForge() {
        return forge;
    }
}
