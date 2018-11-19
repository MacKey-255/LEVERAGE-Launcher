package leverage.client;

import javafx.scene.control.Alert;
import leverage.Console;
import leverage.Kernel;
import leverage.auth.user.User;
import leverage.client.components.Mod;
import leverage.client.components.ResourcePack;
import leverage.exceptions.AuthenticationException;
import leverage.exceptions.CheatsDetectedException;
import leverage.exceptions.GameLauncherException;
import leverage.game.profile.Profile;
import leverage.game.version.Version;
import leverage.game.version.VersionMeta;
import leverage.game.version.Versions;
import leverage.gui.lang.Language;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AntiCheat {
    private Kernel kernel;
    private Console console;

    private Version versionLocal;
    private List<Mod> modsLocal;
    private List<ResourcePack> resourceLocal;

    private boolean accept;

    public AntiCheat(Kernel kernel) {
        this.kernel = kernel;
        this.console = kernel.getConsole();
        this.accept = false;
    }

    public void loadVersions() {
        // Buscar version Selecionada
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
        // Cargar Listado del Antiparches (Mods y Versiones)
        console.print("Cargando Mods Locales.");
        modsLocal = kernel.loadMods();      // Generar Mods
        loadVersions();                     // Generar Version
    }

    public void sendServerList() {
        try {
            // Tomar datos generados en Ficheros JSON
            File modFile = new File(Kernel.APPLICATION_LIBS, "mods_list.json");
            File versionFile = new File(Kernel.APPLICATION_LIBS, "version.json");

            String modString = new String(Files.readAllBytes(modFile.toPath()), StandardCharsets.UTF_8);
            String versionString = new String(Files.readAllBytes(versionFile.toPath()), StandardCharsets.UTF_8);

            JSONArray modsList = new JSONArray(modString);
            JSONObject versionObject = new JSONObject(versionString);

            // Añadir contenido al Request

            Map<String, String> postParamsMods = new HashMap<>();           // Mods
            Map<String, String> postParamsVersion = new HashMap<>();          // Version
            postParamsMods.put("Content-Type", "application/json; charset=utf-8");
            postParamsMods.put("Content-Length", String.valueOf(modsList.toString().length()));

            postParamsVersion.put("Content-Type", "application/json; charset=utf-8");
            postParamsVersion.put("Content-Length", String.valueOf(versionObject.toString().length()));

            // Enviar Informacion en modo POST
            String responseMods = Utils.sendPost(Urls.modsList, modsList.toString().getBytes(Charset.forName("UTF-8")), postParamsMods);
            String responseVersion = Utils.sendPost(Urls.modsList, versionObject.toString().getBytes(Charset.forName("UTF-8")), postParamsVersion);

            console.print(responseMods);

            //Tomar Informacion
            JSONObject entryMods = null;
            JSONObject entryVersion = null;

            if (responseMods.isEmpty())
                console.print("El Servidor no ha devuelto ninguna respuesta sobre los Mods.");
            else
                entryMods = new JSONObject(responseMods);
            if (responseVersion.isEmpty())
                console.print("El Servidor no ha devuelto ninguna respuesta sobre la Version.");
            else
                entryVersion = new JSONObject(responseVersion);

            if(entryMods != null && entryVersion != null) {
                if (entryMods.getBoolean("error") && entryVersion.getBoolean("error")) {
                    console.print("Los mods y la version de su cliente concuerdan con el Servidor.");
                    accept = true;
                } else {
                    console.print("Existe una diferencia en los mods o en la Version.");
                    accept = false;
                    // Buscamos el Problema y lo mostramos
                    String request = "Existe una diferencia en los ";
                    if(!entryMods.getBoolean("error"))
                        request += "Mods: " + entryMods.getString("request");
                    if(!entryVersion.getBoolean("error"))
                        request += "y en la Version: " + entryVersion.getString("request");

                    boolean cheatDetected = false;
                    if("CHEAT".equals(request)) {
                        cheatDetected = true;
                        request = "Parche detectado!!";
                    }

                    throw new CheatsDetectedException(request, cheatDetected);
                }
            } else {
                console.print("Existe una diferencia en los mods o en la Version.");
                kernel.showAlert(Alert.AlertType.ERROR, null, "Existe una diferencia en los mods o en la Version.");
                accept = false;
            }

        } catch (CheatsDetectedException ex) {
            console.print("No se ha podido Comprobar con el Servidor las Versiones");
            kernel.showAlert(Alert.AlertType.ERROR, null, "No se ha podido Comprobar con el Servidor las Versiones");
            console.print(ex.getMessage());

            ex.printStackTrace();
            accept = false;
        } catch (IOException e) {
            e.printStackTrace();
            console.print(e.getMessage());
        } catch (JSONException j) {
            console.print("El Servidor no ha respondido correctamente!");
            accept = false;
        }
    }

    // Via RCON -- Whitelist
    public static boolean addWhiteListRCON(String username) throws AuthenticationException, IOException {
        return (null != Utils.rconAction("whitelist add "+ username));
    }

    // Via RCON -- Whitelist
    public static boolean removeWhiteListRCON(String username) throws AuthenticationException, IOException {
        return (null != Utils.rconAction("whitelist remove "+ username));
    }

    // Via Web -- Whitelist
    public static void addWhiteList(String uuid) throws IOException, GameLauncherException {
        String path = Urls.whitelist, r = null;
        Map<String, String> params = new HashMap<>();
        params.put("Access-Token", uuid);
        params.put("Client-Token", uuid);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");

        r = Utils.sendPost(path, null, params);

        if ("OK".equals(r)) {
            System.out.println('[' + dateFormat.format(new Date()) + " -- Web] " + "Usted ha entrado en la Lista Blanca del Servidor!");
        } else {
            System.out.println('[' + dateFormat.format(new Date()) + " -- Web] " + r);
            if(!"ERROR YA USTED ESTA EN LA LISTA BLANCA.".equals(r))
                throw new GameLauncherException("El Servidor no ha enviado Informacion Incorrecta.");
        }
    }

    // Via Web -- Whitelist
    public static void removeWhiteList(String uuid) throws GameLauncherException {
        String path = Urls.blacklist, r = null;
        Map<String, String> params = new HashMap<>();
        params.put("Access-Token", uuid);
        params.put("Client-Token", uuid);
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd -- HH:mm:ss");
        try {
            r = Utils.sendPost(path, null, params);
            if ("OK".equals(r)) {
                System.out.println('[' + dateFormat.format(new Date()) + " -- Web] " + "Usted ha salido de la Lista Blanca del Servidor!");
            } else {
                System.out.println('[' + dateFormat.format(new Date()) + " -- Web] " + r);
                if(!"ERROR USTED NO ESTA EN LA LISTA BLANCA.".equals(r))
                    throw new GameLauncherException("El Servidor no ha enviado Informacion Incorrecta.");
            }
        } catch (IOException ex) {
            System.out.println('[' + dateFormat.format(new Date()) + "] " + r);
        }
    }

    private void writeJSON() {
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
            File file = new File(Kernel.APPLICATION_WORKING_DIR, versionLocal.getRelativeJar().getPath());
            object.put("id", versionLocal.getID());
            object.put("diskSpace", file.length());
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
        this.generateList();        // Genero lista Local
        this.writeJSON();           // Guardo Listados
        this.sendServerList();      // Envio los Listados en JSON
    }

    public boolean isAccept() {
        return true;
    }
}
