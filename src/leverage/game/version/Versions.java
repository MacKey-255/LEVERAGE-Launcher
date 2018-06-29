package leverage.game.version;

import leverage.Console;
import leverage.Kernel;
import leverage.util.Utils;
import leverage.util.Urls;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class Versions {
    private final Set<VersionMeta> versions = new LinkedHashSet<>();
    private final Collection<Version> version_cache = new HashSet<>();
    private final Console console;
    private final Kernel kernel;
    private VersionMeta latestSnap, latestRel;

    public Versions(Kernel k) {
        kernel = k;
        console = k.getConsole();
    }

    /**
     * Gets the version meta by id
     * @param id The id to fetch the version meta
     * @return The version meta from the specified id or from the latest release
     */
    public final VersionMeta getVersionMeta(String id) {
        if (id != null) {
            switch (id) {
                case "latest-release":
                    return latestRel;
                case "latest-snapshot":
                    return latestSnap;
            }
            for (VersionMeta m : versions) {
                if (m.getID().equalsIgnoreCase(id)) {
                    return m;
                }
            }
        }
        return latestRel;
    }

    /**
     * Gets the version data from a version meta
     * @param vm The target version meta
     * @return The version data from the specified version meta or null if an error happened
     */
    public final Version getVersion(VersionMeta vm) {
        if (versions.contains(vm)) {
            for (Version v : version_cache) {
                if (v.getID().equalsIgnoreCase(vm.getID())) {
                    return v;
                }
            }
            try {
                Version v = new Version(vm.getURL(), kernel);
                version_cache.add(v);
                return v;
            } catch (Exception ex) {
                console.print(ex.getMessage());
                return null;
            }
        }
        console.print("Version id " + vm.getID() + " no encontrada.");
        return null;
    }

    /**
     * Cargar Versiones del Servidor o Locales
     */
    public final void fetchVersions() {
        String lr = "", ls = "";
        console.print("Buscando Listado de Versiones remotas.");
        try {
            String response = Utils.readURL(Urls.versionManifest);
            if (!response.isEmpty()) {
                JSONObject root = new JSONObject(response);
                if (root.has("latest")) {
                    JSONObject latest = root.getJSONObject("latest");
                    if (latest.has("snapshot")) {
                        ls = latest.getString("snapshot");
                    }
                    if (latest.has("release")) {
                        lr = latest.getString("release");
                    }
                }
                JSONArray vers = root.getJSONArray("versions");
                for (int i = 0; i < vers.length(); i++) {
                    JSONObject ver = vers.getJSONObject(i);
                    String id = null;
                    VersionType type;
                    String url = null;
                    if (ver.has("id")) {
                        id = ver.getString("id");
                    }
                    if (ver.has("url")) {
                        url = ver.getString("url");
                    }
                    if (id == null || url == null) {
                        continue;
                    }
                    if (ver.has("type")) {
                        try {
                            type = VersionType.valueOf(ver.getString("type").toUpperCase(Locale.ENGLISH));
                        } catch (IllegalArgumentException ex) {
                            type = VersionType.RELEASE;
                            console.print("Tipo de version invalida: " + id);
                        }
                    } else {
                        type = VersionType.RELEASE;
                        console.print("Version remota " + id + " no es una version. Se ha establecido de tipo RELEASE.");
                    }
                    VersionMeta vm = new VersionMeta(id, url, type);
                    if (lr.equalsIgnoreCase(id)) {
                        latestRel = vm;
                    }
                    if (ls.equalsIgnoreCase(id)) {
                        latestSnap = vm;
                    }
                    versions.add(vm);
                }
                console.print("Listado Remoto de Versiones Cargado.");
            } else {
                console.print("Listado Remoto de Versiones esta vacio.");
            }
        } catch (JSONException ex) {
            console.print("Ha fallado la busqueda del Listado Remoto de Versiones.");
            ex.printStackTrace(console.getWriter());
        }
        console.print("Buscando Listado de Versiones Locales.");
        VersionMeta lastRelease = null, lastSnapshot = null;
        String latestRelease = "", latestSnapshot = "";
        File versionsDir = new File(Kernel.APPLICATION_WORKING_DIR, "versions");
        try {
            if (versionsDir.isDirectory()) {
                File[] files = versionsDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.isDirectory()) {
                            File jsonFile = new File(file.getAbsolutePath(), file.getName() + ".json");
                            if (jsonFile.isFile()) {
                                String id;
                                String url = jsonFile.toURI().toURL().toString();
                                VersionType type;
                                JSONObject ver = new JSONObject(Utils.readURL(url));
                                if (ver.has("id")) {
                                    id = ver.getString("id");
                                } else {
                                    continue;
                                }
                                if (ver.has("type")) {
                                    try {
                                        type = VersionType.valueOf(ver.getString("type").toUpperCase(Locale.ENGLISH));
                                    } catch (IllegalArgumentException ex) {
                                        type = VersionType.RELEASE;
                                        console.print("Tipo de version invalida: " + id);
                                    }
                                } else {
                                    type = VersionType.RELEASE;
                                    console.print("Version remota " + id + " no es una version. Se ha establecido de tipo RELEASE.");
                                }
                                VersionMeta vm = new VersionMeta(id, url, type);
                                versions.add(vm);
                                if (ver.has("releaseTime")) {
                                    if (type == VersionType.RELEASE && ver.getString("releaseTime").compareTo(latestRelease) > 0) {
                                        lastRelease = vm;
                                        latestRelease = ver.getString("releaseTime");
                                    } else if (type == VersionType.SNAPSHOT && ver.getString("releaseTime").compareTo(latestSnapshot) > 0) {
                                        lastSnapshot = vm;
                                        latestSnapshot = ver.getString("releaseTime");
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (latestRel == null && lastRelease != null) {
                latestRel = lastRelease;
            }
            if (latestSnap == null && lastSnapshot != null) {
                latestSnap = lastSnapshot;
            }
            console.print("Listado de Versiones Locales cargado.");
        } catch (JSONException | IOException ex) {
            console.print("Ha fallado la busqueda de Versiones Locales.");
            ex.printStackTrace(console.getWriter());
        }
    }

    /**
     * Returns the version database
     * @return The version database
     */
    public final Iterable<VersionMeta> getVersions() {
        return versions;
    }

    /**
     * Returns the latest release
     * @return The latest release
     */
    public final VersionMeta getLatestRelease() {
        return latestRel;
    }

    /**
     * Returns the latest snapshot
     * @return The latest snapshot
     */
    public final VersionMeta getLatestSnapshot() {
        return latestSnap;
    }
}
