package leverage.client;

import leverage.Console;
import leverage.Kernel;
import leverage.client.components.Mod;
import leverage.client.components.VersionServer;
import leverage.game.profile.Profile;
import leverage.game.version.Version;
import leverage.game.version.VersionMeta;
import leverage.game.version.Versions;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class AntiCheat {
    private Kernel kernel;
    private Console console;

    private Version versionLocal;
    private List<Mod> modsLocal;

    private VersionServer versionServer;
    private List<Mod> modsServer;

    public AntiCheat(Kernel kernel) {
        this.kernel = kernel;
        this.console = kernel.getConsole();
    }

    public void loadVersions() {
        Profile p = kernel.getProfiles().getSelectedProfile();

        Versions versions = kernel.getVersions();
        VersionMeta verID;
        switch (p.getType()) {
            case CUSTOM:
                verID = p.hasVersion() ? p.getVersionID() : versions.getLatestRelease();
                break;
            case RELEASE:
                verID = versions.getLatestRelease();
                break;
            default:
                verID = versions.getLatestSnapshot();
                break;
        }
        versionLocal = versions.getVersion(verID);
    }

    public void generateList() {
        // Cargar Listado del Antiparches
        console.print("Cargando Mods Locales.");
        modsLocal = kernel.loadMods();
        loadVersions();
    }

    public void generateServerList() {
        try {
            String text, response = Utils.readURL(Urls.modsList);
            if (response.isEmpty()) {
                console.print("El Servidor no ha devuelto nunguna Lista de Mods.");
                return;
            }
            JSONArray entries = new JSONArray(response);
            for (int i = 0; i < entries.length(); i++) {
                JSONObject entry = entries.getJSONObject(i);

                text = entry.keys().next();
                JSONObject mod = entry.getJSONObject(text);

            }
        } catch (Exception ex) {
            console.print("No se ha podido Cargar los Datos de las Noticias.");
            ex.printStackTrace(console.getWriter());
        }
    }

    public void writeJSON() {
        if(modsLocal.size() != 0) {
            JSONArray array = Utils.getModsJSON(modsLocal);

            if (!Utils.writeToFile(array.toString(), new File(Kernel.APPLICATION_LIBS, "mods_list.json"))) {
                console.print("Ha Fallado la Salva de Mods!");
            } else {
                console.print("Mods Guardados.");
            }
        }

        if(versionLocal != null) {
            JSONObject object = new JSONObject();
            object.put("id", versionLocal.getID());
            object.put("diskSpace", versionLocal.getDiskSpace());
            object.put("version", versionLocal.getJar());
            object.put("vertionType", versionLocal.getType().name());


            if (!Utils.writeToFile(object.toString(), new File(Kernel.APPLICATION_LIBS, "version.json"))) {
                console.print("Ha Fallado la Salva de la Version!");
            } else {
                console.print("Version Guardados.");
            }
        }
    }

    public void compare() {
        // Comparar Cliente y Servidor
        this.generateList();
        this.writeJSON();
    }
}
