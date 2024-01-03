import io.undertow.io.IoCallback;
import io.undertow.io.Sender;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

import java.io.*;
import java.nio.ByteBuffer;

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
        });
    }

    protected abstract void logic(HttpServerExchange exchange) throws Exception;

    static FileData prepareFileData(String linkPath) throws Exception {
        String filepath = Main.rootPath + linkPath
                .replaceAll("\\\\", "/")
                .replaceAll("\\.\\./", "");
        File file = new File(filepath);
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        return new FileData(filename, new FileInputStream(file), file.length());
    }

    static void send(HttpServerExchange exchange, String contents) throws IOException {
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, contents.length());
        Sender sender = exchange.getResponseSender();
        sender.send(contents, new CustomIoCallback());
    }

    static void send(HttpServerExchange exchange, FileData fileData) throws IOException {
        exchange.getResponseHeaders().put(Headers.CONTENT_LENGTH, fileData.contentLength);
        Sender sender = exchange.getResponseSender();
        send(sender, fileData.contents);
    }

    static void send(Sender sender, FileInputStream source) {
        send(sender, source, false);
    }

    static void send(Sender sender, FileInputStream source, boolean isLast) {
        byte[] contents;
        ByteBuffer buffer;

        try {
            contents = new byte[Math.min(source.available(), 4096)];
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            if (source.read(contents) == -1) throw new IOException("Over-read file! Report to dev.");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        buffer = ByteBuffer.wrap(contents);

        if (isLast) {
            sender.send(buffer, new CustomIoCallback());
            return;
        }

        sender.send(buffer, new IoCallback() {
            @Override
            public void onComplete(HttpServerExchange exchange, Sender sender) {
                try {
                    send(sender, source, source.available() == 0);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public void onException(HttpServerExchange exchange, Sender sender, IOException exception) {
                Main.processesRunning.decrementAndGet();
                exchange.endExchange();
                throw new RuntimeException(exception);
            }
        });
    }

    static String getMimeType(String filename) {
        switch (filename.substring(filename.lastIndexOf(".") + 1)) {
            case "txt":
            case "log":
            case "conf":
                return "text/plain";
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
