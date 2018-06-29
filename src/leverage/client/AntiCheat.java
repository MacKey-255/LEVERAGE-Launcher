package leverage.client;

import leverage.Console;
import leverage.Kernel;
import leverage.client.components.Mod;
import leverage.client.components.ResourcePack;
import leverage.game.profile.Profile;
import leverage.game.version.Version;
import leverage.util.Utils;
import org.json.JSONArray;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AntiCheat {
    private Kernel kernel;
    private Console console;
    private List<Mod> mods;
    private Version version;
    private List<ResourcePack> packs;

    public AntiCheat(Kernel kernel) {
        this.kernel = kernel;
        this.console = kernel.getConsole();

        mods = new ArrayList<>();
        packs = new ArrayList<>();
    }

    public void generateList() {
        // Cargar Listado del Antiparches
        console.print("Cargando Mods Locales.");
        mods = kernel.loadMods();

        console.print("Cargando ResourcePack Locales.");
        mods = kernel.loadMods();
    }

    public void getServerListMods() {
        // Cargar Listado de Mods Local
        console.print("Cargando Mods Locales.");
        List<Mod> mods = kernel.loadMods();
    }

    public void addPacks(ResourcePack packs) {
        this.packs.add(packs);
    }

    public void writeJSON() {
        if(mods.size() != 0) {
            // Enviando Lista de Mods al Servidor
            JSONArray array = Utils.getModsJSON(mods);

            if (!Utils.writeToFile(array.toString(), new File(Kernel.APPLICATION_LIBS, "mods_list.json"))) {
                console.print("Ha Fallado la Salva de Mods!");
            } else {
                console.print("Mods Guardados.");
            }
        }
    }

    public void compare() {
        // Comparar Cliente y Servidor
        this.generateList();
        this.writeJSON();

        Profile p = kernel.getProfiles().getSelectedProfile();
    }
}
