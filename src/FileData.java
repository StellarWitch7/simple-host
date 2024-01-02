public class FileData {
    public final String name;
    public final byte[] contents;
    public final int contentLength;

    public FileData(String name, byte[] contents) {
        this.name = name;
        this.contents = contents;
        this.contentLength = contents.length;
    }
}
