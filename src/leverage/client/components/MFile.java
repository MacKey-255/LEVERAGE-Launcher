package leverage.client.components;

import leverage.util.Hashed;

import java.io.File;

public class MFile {

    private String id;
    private String url;
    private String fileHash;
    private final File file;

    public MFile(String id, String url) {
        this.id = id;
        this.url = url;
        this.file = new File(url);
        this.fileHash = Hashed.generateSHA1(this.file);
    }

    public MFile(File file) {
        this.id = file.getName();
        this.url = file.getPath();
        this.file = file;
        this.fileHash = Hashed.generateSHA1(this.file);
    }

    public String getId() {
        return id;
    }

    public String getUrl() {
        return url;
    }

    public String getFileHash() {
        return fileHash;
    }

    public File getFile() {
        return file;
    }
}
