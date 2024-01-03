import java.io.FileInputStream;

public class FileData {
    public final String name;
    public final FileInputStream contents;
    public final long contentLength;

    public FileData(String name, FileInputStream contents, long contentLength) {
        this.name = name;
        this.contents = contents;
        this.contentLength = contentLength;
    }
}
