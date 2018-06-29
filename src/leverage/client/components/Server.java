package leverage.client.components;

import leverage.client.extra.StatusType;
import leverage.game.version.Version;

import java.util.List;

public class Server {

    private StatusType status;
    private String name;
    private String url;
    private int players;
    private int maxPlayers;

    private List<Mod> modslist;
    private Version version;

    public Server(StatusType status, String name, String url, int players, int maxPlayers, List<Mod> modslist, Version version) {
        this.status = status;
        this.name = name;
        this.url = url;
        this.players = players;
        this.maxPlayers = maxPlayers;

        this.modslist = modslist;
        this.version = version;
    }

    public Server(StatusType status, String name, String url, int players, int maxPlayers, Version version) {
        this.status = status;
        this.name = name;
        this.url = url;
        this.players = players;
        this.maxPlayers = maxPlayers;

        this.version = version;
    }

    public Server(String name, String url, List<Mod> modslist, Version version) {
        this.status = StatusType.OFFLINE;
        this.name = name;
        this.url = url;
        this.players = 0;
        this.maxPlayers = 0;

        this.modslist = modslist;
        this.version = version;
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
}
