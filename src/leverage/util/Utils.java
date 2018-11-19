package leverage.util;

import leverage.Kernel;
import leverage.OS;
import leverage.OSArch;
import leverage.client.components.Mod;
import leverage.exceptions.AuthenticationException;
import leverage.exceptions.CheatsDetectedException;
import leverage.rcon.Rcon;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.xml.bind.DatatypeConverter;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.jar.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public final class Utils {
    /**
     * Gets the current operating system
     * @return An OS enum with the detected OS
     */
    public static OS getPlatform() {
        String osName = System.getProperty("os.name").toLowerCase(Locale.ENGLISH);
        if (osName.contains("win")) {
            return OS.WINDOWS;
        }
        if (osName.contains("mac")) {
            return OS.OSX;
        }
        if (osName.contains("linux") || osName.contains("unix")) {
            return OS.LINUX;
        }
        return OS.UNKNOWN;
    }

    /**
     * Gets the default working directory for the launcher depending of the OS
     * @return The working directory
     */
    public static File getWorkingDirectory() {
        String userHome = System.getProperty("user.home", ".");
        File workingDirectory;
        switch (getPlatform()) {
            case LINUX:
                workingDirectory = new File(userHome, ".minecraft/");
                break;
            case WINDOWS:
                String applicationData = System.getenv("APPDATA");
                String folder = applicationData != null ? applicationData : userHome;
                workingDirectory = new File(folder, ".minecraft/");
                break;
            case OSX:
                workingDirectory = new File(userHome, "Library/Application Support/minecraft");
                break;
            default:
                workingDirectory = new File(userHome, "minecraft/");
        }
        return workingDirectory;
    }

    /**
     * Deletes a directory recursively
     * @param directory The target directory
     */
    public static void deleteDirectory(File directory) {
        if (directory.isDirectory()) {
            File[] files = directory.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (f.isDirectory())
                        deleteDirectory(f);
                    else
                        f.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Downloads a file and even caches it if server ETAG is existent
     * @param url The source URL
     * @throws IOException When data read fails
     * @param output The output file
     */
    public static void downloadFile(String url, File output) throws IOException {
        if (url.startsWith("file")) {
            return;
        }
        URLConnection con = new URL(url).openConnection();
        String ETag = con.getHeaderField("ETag");
        //Match ETAG with existing file
        if (ETag != null) {
            if (output.isFile() && verifyChecksum(output, ETag.replace("\"", ""), "MD5")) {
                return;
            }
        }
        File parent = output.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        pipeStreams(con.getInputStream(), new FileOutputStream(output));
    }

    private static String readText(InputStream is) {
        if (is == null) {
            return "";
        }
        StringBuilder content = new StringBuilder();
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")))) {
            String line;
            boolean first = true;
            while ((line = bufferedReader.readLine()) != null) {
                if (!first) {
                    content.append(System.lineSeparator());
                } else {
                    first = false;
                }
                content.append(line);
            }
            return content.toString();
        } catch (IOException ex) {
            return "";
        }
    }

    private static void pipeStreams(InputStream in, OutputStream out) {
        if (in == null || out == null) {
            return;
        }
        try {
            byte[] buffer = new byte[8192];
            int read;
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        } catch (IOException ignored) {
            //No problem
        } finally {
            try {
                in.close();
            } catch (IOException ignored) {
                //Nothing we can do
            }
            try {
                out.close();
            } catch (IOException ignored) {
                //Nothing we can do
            }
        }
    }

    public static InputStream readCachedStream(String url) {
        try {
            URLConnection con = new URL(url).openConnection();
            String ETag = con.getHeaderField("ETag");
            if (ETag != null) {
                ETag = ETag.replace("\"", "");
                File cachedFile = new File(Kernel.APPLICATION_CACHE, ETag);
                if (!cachedFile.isFile() || cachedFile.length() != con.getContentLength()) {
                    return new CachedInputStream(con.getInputStream(), cachedFile);
                }
                return new FileInputStream(cachedFile);
            }
            return con.getInputStream();
        } catch (IOException ex) {
            return null;
        }
    }

    /**
     * Reads a String from the source URL
     * @param url The source URL
     * @return The read String or null if an error occurred
     */
    public static String readURL(String url) {
        InputStream in = readCachedStream(url);
        if (in != null) {
            return readText(in);
        }
        return "";
    }

    /**
     * Verifies a checksum from a file
     * @param file The file to be checked
     * @param hash The hash or checksum to check
     * @param method The hash format (md5, sha1...)
     * @return A boolean that indicated if the hash matches
     */
    public static boolean verifyChecksum(File file, String hash, String method) {
        if (hash == null || method == null || !file.isFile()) {
            return false;
        }
        String fileHash = calculateChecksum(file, method);
        return hash.equals(fileHash);
    }

    /**
     * Calculates a checksum from a File
     * @param file The input File
     * @param algorithm The hash method (md5, sha1...)
     * @return The calculated hash
     */
    private static String calculateChecksum(File file, String algorithm) {
        try (FileInputStream fis = new FileInputStream(file)){
            MessageDigest sha1 = MessageDigest.getInstance(algorithm);
            byte[] data = new byte[8192];
            int read;
            while ((read = fis.read(data)) != -1) {
                sha1.update(data, 0, read);
            }
            byte[] hashBytes = sha1.digest();
            StringBuilder sb = new StringBuilder();
            for (byte hashByte : hashBytes) {
                sb.append(Integer.toString((hashByte & 0xff) + 0x100, 16).substring(1));
            }
            return sb.toString();
        } catch (IOException | NoSuchAlgorithmException ex) {
            return null;
        }
    }

    /**
     * Return List JSONArray Mods List
     * @param mods mods Mods List
     * @return A List Mods
     */
    public static JSONArray getModsJSON(List<Mod> mods) {
        JSONArray list = new JSONArray();
            JSONObject mod, o;
            for(int i=0; i<mods.size(); i++) {
                mod = new JSONObject();
                o = new JSONObject();

                o.put("id", mods.get(i).getId());
                o.put("nameJar", mods.get(i).getNameJar());
                o.put("version", mods.get(i).getVersion());
                o.put("diskSpace", mods.get(i).getdiskSpace());
                o.put("vmc", mods.get(i).gerVMC());

                mod.put(mods.get(i).getName(), o);

                list.put(i, mod);
            }
        return list;
    }

    /**
     * Reader a List Mods
     * @param m The Mods Dir
     * @return A List Mods
     */
    public static List<Mod> getListMods(File m) throws IOException, CheatsDetectedException {
        List<Mod> list = new ArrayList<>();
        if (m.isDirectory()) {
            File[] h = m.listFiles();
            for (int i=0; i < h.length; i++) {
                List<Mod> tmp = getListMods(h[i]);
                for(int j=0; j<tmp.size(); j++) {
                    Mod mod = tmp.get(j);
                    if(mod.getName() != null)
                        list.add(tmp.get(j));
                }
            }
        } else {
            Mod mod = getMod(m);
            if(mod.getName() != null)
                list.add(mod);
        }
        return list;
    }

    /**
     * Reader a Mod File
     * @param file The Mod File
     * @return A Mod
     */
    public static Mod getMod(File file) throws IOException, CheatsDetectedException {
        String id = null, nam = null, version = null, vmc = null;
        String url = file.getAbsolutePath();
        String nameJar = file.getName();

        //Abrir Archivo y Leerlo
        JarInputStream input = new JarInputStream(file.toURI().toURL().openStream());
        int v; String name, mod_name = file.getName().replace(".jar", "");

        //Buscar Informacion: mcmod.info
        for(JarEntry jar = input.getNextJarEntry(); jar != null; jar = input.getNextJarEntry()) {
            if(!jar.isDirectory()) {
                name = jar.getName();
                if (name.equals("mcmod.info")) {
                    //Copiamos el Fichero en el Cache
                    File cache = new File(Kernel.APPLICATION_CACHE, "mods"); //new File(Kernel.APPLICATION_CACHE.getPath()+"/mods/");
                    if(!cache.exists())
                        cache.mkdirs();

                    FileOutputStream ou = new FileOutputStream(cache.getPath() + File.separator + mod_name + ".json");
                    BufferedOutputStream buffout = new BufferedOutputStream(ou);
                    BufferedInputStream buffin = new BufferedInputStream(input);
                    while ((v = buffin.read()) != -1) {
                        buffout.write(v);
                    }
                    buffout.flush();

                    //Leer Fichero del Cache
                    try {
                        File config = new File(cache.getPath() + File.separator + mod_name + ".json");
                        String data = new String(Files.readAllBytes(config.toPath()), StandardCharsets.UTF_8);

                        JSONArray array = new JSONArray(data);
                        JSONObject object = array.getJSONObject(0);

                        id = object.getString("modid");
                        nam = object.getString("name");

                        //Detectar Cheats Especificos (Wurst)
                        if(id.equals("forgewurst") || id.equals("xray"))
                            throw new CheatsDetectedException(jar.getName(), true);

                        try {
                            version = object.getString("version");
                        } catch (JSONException ex) {
                            version = "?";
                        }
                        try {
                            vmc = object.getString("mcversion");
                            if(vmc.equals("${mcversion}") || vmc.equals("${version}"))
                                vmc = "?";
                        } catch (JSONException ex) {
                            vmc = "?";
                        }
                    } catch (JSONException ex) {
                        System.out.println("Mods PreCargado: "+ mod_name );
                    }

                    break ;
                } else {
                    // Valor por defecto ( Si no encuentra el mcmod.info entonces se asigna esto)
                    nam = mod_name; id = mod_name; nameJar = mod_name; version = "?"; vmc = "?";
                }
            } else {
                //Detectar Cheats Especificos ( XRay )
                if(jar.getName().equals("minecraftxray") || jar.getName().equals("xray"))
                    throw new CheatsDetectedException(jar.getName(), true);
            }
        }
        return new Mod(id, nam, url, nameJar, version, vmc);
    }

    /**
     * Writes a String to a File
     * @param o The String to be written
     * @param f The output File
     * @return A boolean that indicated if the text has been written
     */
    public static boolean writeToFile(String o, File f) {
        try (FileOutputStream out = new FileOutputStream(f)) {
            if (!f.getParentFile().exists()) {
                f.getParentFile().mkdirs();
            }
            out.write(o.getBytes(StandardCharsets.UTF_8));
            return true;
        } catch (IOException ex) {
            return false;
        }
    }

    /**
     * Checks whether the current OS arch is 32 or 64 bits
     * @return An OSArch enum with the detected architecture
     */
    public static OSArch getOSArch() {
        String arch = System.getProperty("os.arch");
        String realArch = arch.endsWith("64") ? "64" : "32";
        return "32".equals(realArch) ? OSArch.OLD : OSArch.NEW;
    }

    /**
     * Gets a real path from an artifact path
     * @param artifact The artifact path
     * @param ext The extension
     * @return The real path
     */
    public static String getArtifactPath(String artifact, String ext) {
        String[] parts = artifact.split(":", 3);
        return String.format("%s/%s/%s/%s." + ext, parts[0].replaceAll("\\.", "/"), parts[1], parts[2], parts[1] + '-' + parts[2]);
    }

    /**
     * Sends a post to the desired target
     * @param url The POST URL
     * @param data The data to be sent
     * @param params The headers to be sent
     * @return The response of the server
     * @throws IOException If connection failed
     */
    public static String sendPost(String url, byte[] data, Map<String, String> params) throws IOException {
        HttpURLConnection con = (HttpURLConnection)new URL(url).openConnection();
        con.setDoOutput(true);
        con.setRequestMethod("POST");
        if (!params.isEmpty()) {
            Set keys = params.keySet();
            for (Object key : keys) {
                String param = key.toString();
                con.setRequestProperty(param, params.get(param));
            }
        }
        if (data != null) {
            try (OutputStream out = con.getOutputStream()) {
                out.write(data);
            }
        }
        if (con.getResponseCode() == 200) {
            return readText(con.getInputStream());
        } else {
            return readText(con.getErrorStream());
        }
    }

    /**
    * Get UUID for Player Username
    * @return UUID
    */
    public static String getUUID(String playername) {
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + playername).getBytes());
        return uuid.toString();
    }

    /**
     * Get Volumen String for Volumen Double
     * @return Volumen
     */
    public String calculateVolumen(double vol) {
        DecimalFormat df = new DecimalFormat("#.00");

        if(vol>1024000000)
            return df.format(vol/1024000000) + " GB";
        else if(vol>1024000)
            return df.format(vol/1024000) + " MB";
        else if(vol>1024)
            return df.format(vol/1024) + " Kb";
        else
            return df.format(vol) + " Bytes";
    }

    /**
     * Gets the java executable path
     * @return The java executable path
     */
    public static String getJavaDir() {
        String separator = System.getProperty("file.separator");
        String path = System.getProperty("java.home") + separator + "bin" + separator;
        if (getPlatform() == OS.WINDOWS && new File(path + "javaw.exe").isFile()) {
            return path + "javaw.exe";
        }
        return path + "java";
    }

    /**
     * Converts a Base64 String to text
     * @param st The Base64 String
     * @return The decoded text
     */
    public static String fromBase64(String st) {
        if (st == null || st.isEmpty()) {
            return null;
        }
        String conversion;
        try {
            conversion = new String(DatatypeConverter.parseBase64Binary(st), StandardCharsets.UTF_8);
        } catch (IllegalArgumentException ex) {
            conversion = "";
        }
        return conversion;
    }


    /**
     * Decompresses a ZIP file
     * @param input The ZIP file
     * @param output The output directory
     * @param exclusions Any extraction exclusions
     * @throws IOException If the process failed
     */
    public static void decompressZIP(InputStream input, File output, Iterable<String> exclusions) throws IOException {
        if(!output.isDirectory()){
            output.mkdirs();
        }
        try (ZipInputStream zis = new ZipInputStream(input)){
            ZipEntry ze = zis.getNextEntry();
            while(ze != null){
                String fileName = ze.getName();
                File newFile = new File(output + File.separator + fileName);
                if (ze.isDirectory()) {
                    newFile.mkdir();
                } else {
                    boolean excluded = false;
                    if (exclusions != null) {
                        for (String e : exclusions) {
                            if (fileName.startsWith(e)) {
                                excluded = true;
                            }
                        }
                    }
                    if (excluded) {
                        zis.closeEntry();
                        ze = zis.getNextEntry();
                        continue;
                    }
                    byte[] buffer = new byte[16384];
                    new File(newFile.getParent()).mkdirs();
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zis.closeEntry();
                ze = zis.getNextEntry();
            }
            new File(output, "OK").createNewFile();
        }
    }

    public static String rconAction(String command) throws AuthenticationException, IOException {
        // Connects to 127.0.0.1 on port 27015
        Rcon rcon = null; String value = null;
        rcon = new Rcon(Urls.leverage.replace("http://", ""), Urls.rconPort, "minecraft122".getBytes());
        value = rcon.command(command);

        // Devuelve Resultado de la Peticion RCON
        return value;
    }

    public static void restartApplication() {
        try {
            System.out.println("Reiniciando Launcher");
            final String javaBin = new File(Kernel.APPLICATION_WORKING_DIR, "LEVERAGE-Launcher.jar").getAbsolutePath();
            Runtime rt = Runtime.getRuntime();
            rt.exec(javaBin);
            System.exit(0);
        } catch (Exception e){
            System.out.println("Ha fallado el reinicio del Programa!");
        }
    }
}
