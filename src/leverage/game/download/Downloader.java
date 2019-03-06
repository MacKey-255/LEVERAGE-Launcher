package leverage.game.download;

import leverage.Console;
import leverage.Kernel;
import leverage.exceptions.DownloaderException;
import leverage.game.profile.Profile;
import leverage.game.version.Version;
import leverage.game.version.VersionMeta;
import leverage.game.version.Versions;
import leverage.game.version.asset.AssetIndex;
import leverage.game.version.library.Library;
import leverage.util.Urls;
import leverage.util.Utils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;


public class Downloader {
    private final Console console;
    private final Kernel kernel;
    private double downloaded, validated, total;
    private boolean downloading;
    private String currentFile = "";
    private final int DOWNLOAD_TRIES = 5;

    public Downloader(Kernel k) {
        kernel = k;
        console = k.getConsole();
    }

    /**
     * Downloads all requires game files
     * @throws DownloaderException If the download fails
     */
    public final void download() throws DownloaderException {
        //Initial values
        downloading = true;
        downloaded = 0;
        validated = 0;
        total = 0;
        int tries;

        console.print("Trabajando en la Descarga.");
        if (Kernel.USE_LOCAL) {
            console.print("Estas en modo Offline.");
            downloading = false;
            return;
        }

        //Fetch version used by profile
        Profile p = kernel.getProfiles().getSelectedProfile();
        Versions versions = kernel.getVersions();
        VersionMeta verID = Utils.getVersionMeta(p, versions);
        if (verID == null) {
            downloading = false;
            throw new DownloaderException("Version ID es nula.");
        }
        console.print("Usando version ID: " + verID);
        Version v = versions.getVersion(verID);
        if (v == null) {
            downloading = false;
            throw new DownloaderException("Version info could not be obtained.");
        }

        //Get required files to be downloaded
        Set<Downloadable> urls = new HashSet<>();

        //Fetch assets
        console.print("Entregando urls de Assets...");
        AssetIndex index = v.getAssetIndex();
        File indexJSON = new File(Kernel.APPLICATION_WORKING_DIR, "assets" + File.separator + "indexes" + File.separator + index.getID() + ".json");
        tries = 0;
        if (!Utils.verifyChecksum(indexJSON, index.getSHA1(), "SHA-1")) {
            while (tries < DOWNLOAD_TRIES) {
                try {
                    console.print(index.getURL());
                    Utils.downloadFile(index.getURL(), indexJSON);
                    break;
                } catch (IOException ex) {
                    console.print("Fallida la Descargan del Archivo " + indexJSON.getName() + " (Intento " + tries + ')');
                    //ex.printStackTrace(console.getWriter());
                    tries++;
                }
            }
        }
        if (tries == DOWNLOAD_TRIES) {
            console.print("Fallida la Descargan de los Assets en la Version: " + index.getID());
        } else {
            //Load assets
            try {
                JSONObject root;
                try {
                    root = new JSONObject(new String(Files.readAllBytes(indexJSON.toPath()), StandardCharsets.UTF_8));
                } catch (JSONException | IOException ex) {
                    downloading = false;
                    throw new DownloaderException("Fallida la lectura de los Archivos JSON de Assets.");
                }
                JSONObject objects = root.getJSONObject("objects");
                Set<String> keys = objects.keySet();
                Collection<String> processedHashes = new ArrayList<>();
                File objectsRoot = new File("assets" + File.separator + "objects");
                for (String key : keys) {
                    JSONObject o = objects.getJSONObject(key);
                    String hash = o.getString("hash");
                    long size = o.getLong("size");
                    //String downloadURL = "http://resources.download.minecraft.net/" + hash.substring(0, 2) + '/' + hash;
                    String downloadURL = Urls.assetsPath("objects/" + hash.substring(0, 2) + '/' + hash);
                    File relPath = new File(objectsRoot, hash.substring(0, 2) + File.separator + hash);
                    File fullPath = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + relPath);
                    if (!processedHashes.contains(hash)) {
                        total += size;
                        processedHashes.add(hash);
                        if (!Utils.verifyChecksum(fullPath, hash, "SHA-1")) {
                            Downloadable d = new Downloadable(downloadURL, size, relPath, hash, key);
                            urls.add(d);
                        } else {
                            validated += size;
                        }
                    }
                }
            } catch (JSONException ex) {
                console.print("Failed to parse asset index.");
            }
        }

        //Fetch version
        console.print("Entregando urls de Versiones..");
        Map<String, Downloadable> downloads = v.getDownloads();
        if (downloads.containsKey("client")) {
            Downloadable d = downloads.get("client");
            if (d.hasURL()) {
                long jarSize = d.getSize();
                String jarSHA1 = d.getHash();
                total += d.getSize();
                File destPath = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJar());
                File jsonFile = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + v.getRelativeJSON());
                tries = 0;
                while (tries < DOWNLOAD_TRIES) {
                    try {
                        Utils.downloadFile(v.getJSONURL(), jsonFile);
                        break;
                    } catch (IOException ex) {
                        console.print("Failed to download file " + jsonFile.getName() + " (Intento: " + tries + ')');
                        ex.printStackTrace(console.getWriter());
                        tries++;
                    }
                }
                if (tries == DOWNLOAD_TRIES) {
                    console.print("Failed to download version index " + destPath.getName());
                }
                if (!Utils.verifyChecksum(destPath, jarSHA1, "SHA-1")) {
                    urls.add(d);
                } else {
                    validated += jarSize;
                }
            } else {
                console.print("Version Descargada Incompatible.");
            }
        } else if (v.hasJar()) {
            String jar = v.getJar();
            File relPath = v.getRelativeJar();
            console.print("Found legacy version " + jar);
            if (!relPath.exists()) {
                // "https://s3.amazonaws.com/Minecraft.Download/versions/" + jar + "/" + jar + ".jar"
                Downloadable d = new Downloadable(Urls.versionsPath(jar + "/" + jar + ".jar"), -1, v.getRelativeJar(), null, null);
                urls.add(d);
            } else {
                console.print("Legacy version file found. Assuming is valid.");
            }
        } else {
            console.print("Version file from " + v.getID() + " has no compatible downloadable objects.");
        }


        //Fetch libraries and natives
        console.print("Fetching library and native urls..");
        List<Library> libs = v.getLibraries();
        for (Library lib : libs) {
            if (lib.isCompatible()) {
                //Standard download
                if (lib.hasArtifactDownload()) {
                    Downloadable a = lib.getArtifactDownload();
                    File completePath = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + a.getRelativePath());
                    if (completePath.isFile() && a.getHash() == null) {
                        console.print("File " + completePath + " has no hash. So let's assume the local one is valid.");
                    } else {
                        total += a.getSize();
                        if (Utils.verifyChecksum(completePath, a.getHash(), "SHA-1")) {
                            validated += a.getSize();
                        } else {
                            urls.add(a);
                        }
                    }
                }
                //Native download
                if (lib.hasClassifierDownload()) {
                    Downloadable c = lib.getClassifierDownload();
                    File completePath = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + c.getRelativePath());
                    total += c.getSize();
                    if (completePath.isFile() && c.getHash() == null) {
                        console.print("File " + completePath + " has no hash. So let's assume the local one is valid.");
                    } else {
                        if (Utils.verifyChecksum(completePath, c.getHash(), "SHA-1")) {
                            validated += c.getSize();
                        } else {
                            urls.add(c);
                        }
                    }
                }
            }
        }
        console.print("Descargando Ficheros requeridos por el Juego...");
        if (urls.isEmpty()) {
            console.print("No se pudo descargar nada.");
        } else {
            //Download required files
            //downloadFiles(urls);
        }
        downloading = false;
    }

    /**
     * Performs the download of a Downloadable
     * @param dw The target Downloadable
     */
    private void downloadFile(Downloadable dw) {
        File path = dw.getRelativePath();
        File fullPath = new File(Kernel.APPLICATION_WORKING_DIR + File.separator + path);
        if (fullPath.getParentFile() != null) {
            fullPath.getParentFile().mkdirs();
        }
        if (dw.hasURL()) {
            try {
                URL url = new URL(dw.getURL());
                int tries = 0;
                if (dw.hasFakePath()) {
                    currentFile = dw.getFakePath();
                } else {
                    currentFile = path.toString();
                }
                console.print("Descargando " + currentFile + " desde " + url);
                if (dw.getSize() == 0) {
                    console.print(dw.getURL() + " tamaño del fichero no experado.");
                    try {
                        URLConnection con = url.openConnection();
                        long length = con.getContentLength();
                        total += length;
                    } catch (IOException ex) {
                        console.print("Fallida la determinacion del tamaño de " + dw.getURL());
                    }
                }
                while (tries < DOWNLOAD_TRIES) {
                    int totalRead = 0;
                    try (InputStream in = url.openStream();
                         OutputStream out = new FileOutputStream(fullPath)){
                        byte[] buffer = new byte[8192];
                        int read;
                        while ((read = in.read(buffer)) != -1) {
                            out.write(buffer, 0, read);
                            downloaded += read;
                            totalRead += read;
                        }
                        break;
                    } catch (IOException ex) {
                        console.print("Fallida la Descargan del Archivo " + currentFile + " (Intentar " + tries + ')');
                        ex.printStackTrace(console.getWriter());
                        downloaded -= totalRead;
                        tries++;
                    }
                }
                if (tries == DOWNLOAD_TRIES) {
                    console.print("Fallida la Descargan del Archivo " + path.getName() + " desde " + url);
                }
            } catch (MalformedURLException e) {
                console.print("URL Invalida " + dw.getURL());
                e.printStackTrace(console.getWriter());
            }
        }
    }

    /**
     * Downloads an entire set of Downloadables
     * @param list The set of Downloadables
     */
    private void downloadFiles(Set<Downloadable> list) {
        for (Downloadable d : list) {
            downloadFile(d);
        }
    }

    /**
     * Returns the current download progress
     * @return The download progress
     */
    public final double getProgress() {
        if (!downloading) {
            return 0;
        }
        return (downloaded + validated) / total;
    }

    /**
     * Checks if the download task still running
     * @return A boolean with the current status
     */
    public final boolean isDownloading() {
        return downloading;
    }

    /**
     * Gets the latest file that has been pushed to the download queue
     * @return The current file name
     */
    public final String getCurrentFile() {
        return currentFile;
    }
}