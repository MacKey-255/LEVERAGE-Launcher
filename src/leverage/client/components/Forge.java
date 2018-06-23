package leverage.client.components;

public class Forge extends Version {
    private String id;

    public Forge(String id, String name) {
        super(name);
        this.id = id;
    }

    public Forge(String id, String name, String url, double diskSpace) {
        super(name, url, diskSpace);
        this.id = id;
    }
}
