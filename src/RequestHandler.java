import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.StatusCodes;

import java.io.*;

public abstract class RequestHandler implements HttpHandler {
    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {
        if (Main.isClosing) {
            exchange.setStatusCode(StatusCodes.SERVICE_UNAVAILABLE);
            exchange.endExchange();
            return;
        }

        exchange.dispatch(() -> {
            Main.processesRunning.incrementAndGet();

            try {
                logic(exchange);
            } catch (Exception e) {
                Main.processesRunning.decrementAndGet();
                throw new RuntimeException(e);
            }

            Main.processesRunning.decrementAndGet();
        });
    }

    protected abstract void logic(HttpServerExchange exchange) throws Exception;

    static FileData prepareFileData(String linkPath) throws Exception {
        String filepath = Main.rootPath + linkPath
                .replaceAll("\\\\", "/")
                .replaceAll("\\.\\./", "");
        FileInputStream file = new FileInputStream(filepath);
        DataInputStream dataInputStream = new DataInputStream(file);
        byte[] contents = new byte[file.available()];
        dataInputStream.readFully(contents);
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        return new FileData(filename, contents);
    }

    static String getMimeType(String filename) {
        switch (filename.substring(filename.lastIndexOf(".") + 1)) {
            case "html":
                return "text/html";
            case "css":
                return "text/css";
            case "js":
                return "text/javascript";
            case "json":
                return "application/json";
            case "png":
                return "image/png";
            case "jpg":
            case "jpeg":
                return "image/jpeg";
            case "gif":
                return "image/gif";
            case "txt":
                return "text/plain";
            case "mp4":
                return "video/mp4";
            case "mp3":
                return "audio/mpeg";
            case "weba":
                return "audio/webm";
            case "webm":
                return "video/webm";
            case "webp":
                return "image/webp";
            default:
                return"application/octet-stream";
        }
    }
}
