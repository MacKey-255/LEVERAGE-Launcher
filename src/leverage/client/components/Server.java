package leverage.client.components;

import leverage.client.extra.StatusType;
import leverage.game.version.Version;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONObject;

import java.util.List;

public class Server {

    private StatusType status;
    private String name;
    private int players;
    private int maxPlayers;

    private List<Mod> modslist;
    private Version version;

    // INACTIVO

    public Server(String name, List<Mod> modslist, Version version) {
        this.name = name;
        this.modslist = modslist;
        this.version = version;
        onlineUsers();
    }

    public void onlineUsers() {
        String response = Utils.readURL(Urls.onlineData);
        if (!response.isEmpty()) {
            JSONObject object = new JSONObject(response);
            this.status = StatusType.ONLINE;
            this.players = object.getInt("online");
            this.maxPlayers = object.getInt("total");
        } else {
            this.status = StatusType.OFFLINE;
            this.players = 0;
            this.maxPlayers = 0;
        }
    }

    public StatusType getStatus() {
        return status;
    }

    public String getName() {
        return name;
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
