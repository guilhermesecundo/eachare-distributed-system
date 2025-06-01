package models;

public class Chunk {
    private final int part;
    private final String base64content;

    public Chunk(String base64content, int part) {
        this.base64content = base64content;
        this.part = part;
    }

    public int getPart() {
        return part;
    }

    public String getBase64Content() {
        return base64content;
    }
}
