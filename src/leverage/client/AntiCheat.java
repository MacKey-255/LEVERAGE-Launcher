package leverage.client;

import leverage.Console;
import leverage.Kernel;
import leverage.client.components.Forge;
import leverage.client.components.Mod;
import leverage.client.components.ResourcePack;
import leverage.game.version.Version;

import java.util.ArrayList;
import java.util.List;

public class AntiCheat {
    private Kernel kernel;
    private Console console;
    private List<Mod> mods;
    private List<Forge> forges;
    private List<Version> versions;
    private List<ResourcePack> packs;
    private List<ServerClient> clients;

    public AntiCheat(Kernel kernel) {
        this.kernel = kernel;
        this.console = kernel.getConsole();

        mods = new ArrayList<>();
        forges = new ArrayList<>();
        versions = new ArrayList<>();
        packs = new ArrayList<>();
        clients = new ArrayList<>();
    }
}
