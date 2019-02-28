package leverage.client;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import leverage.Console;
import leverage.Kernel;
import leverage.client.components.MFile;
import leverage.exceptions.AuthenticationException;
import leverage.exceptions.CheatsDetectedException;
import leverage.exceptions.GameLauncherException;
import leverage.game.profile.Profile;
import leverage.game.version.Version;
import leverage.game.version.Versions;
import leverage.util.Hashed;
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
import java.util.*;

public class AntiCheat {
    private Kernel kernel;
    private Console console;

    private Version versionLocal;
    private List<MFile> modsLocal;
    private List<MFile> resourceLocal;

    private boolean accept;

    public AntiCheat(Kernel kernel) {
        this.kernel = kernel;
        this.console = kernel.getConsole();
        this.accept = false;
    }

    private void loadVersions() {
        // Buscar version Selecionada
        Profile p = kernel.getProfiles().getSelectedProfile();
        Versions versions = kernel.getVersions();
        versionLocal = versions.getVersion(Utils.getVersionMeta(p, versions));
    }

    private void generateList() {
        // Cargar Listado del Antiparches (Mods y Versiones)
        console.print("Cargando Mods Locales.");
        modsLocal = kernel.loadMods();              // Generar Mods
        console.print("Cargando Paquetes de Recursos Locales.");
        resourceLocal = kernel.loadResourcePack();  // Generar ResourcePack
        console.print("Cargando Version Local.");
        loadVersions();                             // Generar Version
    }

    private void sendServerList() {
        try {
            // Tomar datos generados en Ficheros JSON
            File modFile = new File(Kernel.APPLICATION_LIBS, "mods_list.json");
            File versionFile = new File(Kernel.APPLICATION_LIBS, "version.json");
            File resourceFile = new File(Kernel.APPLICATION_LIBS, "resource_list.json");

            String modString = new String(Files.readAllBytes(modFile.toPath()), StandardCharsets.UTF_8);
            String versionString = new String(Files.readAllBytes(versionFile.toPath()), StandardCharsets.UTF_8);
            String resourceString = new String(Files.readAllBytes(resourceFile.toPath()), StandardCharsets.UTF_8);

            JSONArray modsList = new JSONArray(modString);
            JSONArray resourceList = new JSONArray(resourceString);
            JSONObject versionObject = new JSONObject(versionString);

            // Añadir contenido al Request
            Map<String, String> postParamsMods = new HashMap<>();           // Mods
            Map<String, String> postParamsResources = new HashMap<>();           // ResourcePack
            Map<String, String> postParamsVersion = new HashMap<>();          // Version

            postParamsMods.put("Content-Type", "application/json; charset=utf-8");
            postParamsMods.put("Content-Length", String.valueOf(modsList.toString().length()));
            postParamsResources.put("Content-Type", "application/json; charset=utf-8");
            postParamsResources.put("Content-Length", String.valueOf(resourceList.toString().length()));
            postParamsVersion.put("Content-Type", "application/json; charset=utf-8");
            postParamsVersion.put("Content-Length", String.valueOf(versionObject.toString().length()));

            // Enviar Informacion en modo POST
            String responseMods = Utils.sendPost(Urls.modsList, modsList.toString().getBytes(Charset.forName("UTF-8")), postParamsMods);
            String responseResources = Utils.sendPost(Urls.resourceList, resourceList.toString().getBytes(Charset.forName("UTF-8")), postParamsResources);
            String responseVersion = Utils.sendPost(Urls.versionList, versionObject.toString().getBytes(Charset.forName("UTF-8")), postParamsVersion);

            /*
            console.print("-------- ----------- ----------");
            console.print("-------- Comprobate ----------");
            console.print("-------- ----------- ----------");
            console.print(Urls.modsList);
            console.print(responseMods);
            console.print("-------- ----------- ----------");
            console.print(Urls.versionList);
            console.print(responseVersion);
            console.print("-------- ----------- ----------");
            console.print(Urls.resourceList);
            console.print(responseResources);
            console.print("-------- ----------- ----------");
            console.print("-------- ----FIN---- ----------");
            console.print("-------- ----------- ----------");
            */

            // Variables Necesarias para la Comprobacion
            JSONObject entry; JSONArray entries;
            boolean errorResponse = false;

            // Comprobar Respuesta (Paquetes de Recursos)
            if (responseResources.isEmpty()) {
                console.print("El Servidor no ha devuelto ninguna respuesta sobre los Paquete de Recursos.");
            } else {
                entry = new JSONObject(responseResources);
                // Comprobar si todo esta OK
                if(entry.getBoolean("error")) {
                    // Comprobar si se detecto algun Parche
                    try {
                        if ("CHEAT".equals(entry.getString("response")))
                            throw new CheatsDetectedException("Parche Detectado!!!", true);
                    } catch (JSONException ignored) {}
                    entries = new JSONArray(entry.get("response").toString());
                    // Mostrar Cartel con los Paquetes de Recursos
                    StringBuilder finalErrorMsg = new StringBuilder();
                    for(int i=0; i<entries.length(); i++) {
                        finalErrorMsg.append(entries.getJSONObject(i).getString("resource"));
                        if(i!=(entries.length()-1))
                            finalErrorMsg.append(", ");
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, "Los ResourcesPack no concuerdan con el Servidor", "ResourcesPack: "+finalErrorMsg.toString());
                        }
                    });
                    errorResponse = true;
                    console.print("Usted posee paquetes de recursos invalidos!");
                }
            }

            // Comprobar Respuesta (Mods)
            if (responseMods.isEmpty()) {
                console.print("El Servidor no ha devuelto ninguna respuesta sobre los Mods.");
            } else {
                entry = new JSONObject(responseMods);
                // Comprobar si todo esta OK
                if(entry.getBoolean("error")) {
                    // Comprobar si se detecto algun Parche
                    try {
                        if("CHEAT".equals(entry.getString("response")))
                            throw new CheatsDetectedException("Parche Detectado!!!", true);
                    } catch (JSONException ignored) {}
                    entries = new JSONArray(entry.get("response").toString());
                    // Mostrar Cartel con los Paquetes de Recursos
                    StringBuilder finalErrorMsg = new StringBuilder();
                    for(int i=0; i<entries.length(); i++) {
                        finalErrorMsg.append(entries.getJSONObject(i).getString("mod"));
                        if(i!=(entries.length()-1))
                            finalErrorMsg.append(", ");
                    }
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, "Los Mods no concuerdan con el Servidor", "Mods: "+finalErrorMsg.toString());
                        }
                    });
                    errorResponse = true;
                    console.print("Usted posee mods invalidos!");
                }
            }

            // Comprobar Respuesta (Version)
            if (responseVersion.isEmpty()) {
                console.print("El Servidor no ha devuelto ninguna respuesta sobre la Version de Minecraft.");
            } else {
                entry = new JSONObject(responseVersion);
                // Comprobar si todo esta OK
                if(entry.getBoolean("error")) {
                    // Comprobar si se detecto algun Parche
                    try {
                        if("CHEAT".equals(entry.getString("response")))
                            throw new CheatsDetectedException("Parche Detectado!!!", true);
                    } catch (JSONException ignored) {}
                    // Mostrar Cartel con la Version
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            kernel.showAlert(Alert.AlertType.ERROR, "La Version no concuerdan con el Servidor", "Version: "+versionObject.getString("id"));
                        }
                    });
                    errorResponse = true;
                    console.print("Usted posee una Version invalida!");
                }
            }

            // Comprobar si huvo algun problema por el camino
            if(!errorResponse) {
                console.print("Los mods, los paquetes de recursos y la version de su cliente concuerdan con el Servidor.");
                accept = true;
            } else {
                console.print("Los mods, los paquetes de recursos y la version de su cliente no concuerdan con el Servidor.");
                accept = false;
            }
        } catch (CheatsDetectedException ex) {
            console.print("Estas baneado del Servidor LEVERAGE de manera Permanente. Bye Bye Parchero!!!");
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    kernel.showAlert(Alert.AlertType.ERROR, "Parche Detectado", ex.getMessage());
                }
            });

            accept = false;
            kernel.exitSafely();
        } catch (IOException e) {
            e.printStackTrace();
            console.print(e.getMessage());
        } catch (JSONException j) {
            j.printStackTrace();
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
                throw new GameLauncherException("El Servidor esta OFFLINE.");
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
                    throw new GameLauncherException("El Servidor esta OFFLINE.");
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
        } else {
            Utils.writeToFile("[]", new File(Kernel.APPLICATION_LIBS, "mods_list.json"));
        }

        if(resourceLocal.size() != 0) {
            JSONArray array = Utils.getResourceJSON(resourceLocal);

            if (!Utils.writeToFile(array.toString(), new File(Kernel.APPLICATION_LIBS, "resource_list.json"))) {
                console.print("Ha Fallado la Salva de Paquetes de Recursos!");
            } else {
                console.print("Paquetes de Recursos Guardados.");
            }
        } else {
            Utils.writeToFile("[]", new File(Kernel.APPLICATION_LIBS, "resource_list.json"));
        }

        if(versionLocal != null) {
            JSONObject object = new JSONObject();
            File file = new File(Kernel.APPLICATION_WORKING_DIR, versionLocal.getRelativeJar().getPath());
            object.put("id", versionLocal.getID());
            object.put("fileHash", Hashed.generateSHA1(file));
            object.put("version", versionLocal.getJar());
            object.put("vertionType", versionLocal.getType().name());


            if (!Utils.writeToFile(object.toString(), new File(Kernel.APPLICATION_LIBS, "version.json"))) {
                console.print("Ha Fallado la Salva de la Version!");
            } else {
                console.print("Version Guardados.");
            }
        } else {
            Utils.writeToFile("[]", new File(Kernel.APPLICATION_LIBS, "version.json"));
        }
    }

    public void compare() {
        // Comparar Cliente y Servidor
        this.generateList();        // Genero lista Local
        this.writeJSON();           // Guardo Listados
        this.sendServerList();      // Envio los Listados en JSON
    }

    public boolean isAccept() {
        return accept;
    }
}
