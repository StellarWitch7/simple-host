import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.StatusCodes;

public class DeliveryHandler extends RequestHandler {
    private final boolean alwaysDownload;

    public DeliveryHandler() {
        alwaysDownload = false;
    }

    public DeliveryHandler(boolean alwaysDownload) {
        this.alwaysDownload = alwaysDownload;
    }

    @Override
    protected void logic(HttpServerExchange exchange) throws Exception {
        FileData fileData;

        try {
            fileData = prepareFileData(exchange.getRelativePath());
        } catch (Exception e) {
            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.endExchange();
            throw new RuntimeException(e);
        }

        if (alwaysDownload) {
            exchange.getResponseHeaders().put(Headers.CONTENT_DISPOSITION, "attachment; filename=" + fileData.name);
        }

        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, getMimeType(fileData.name));
        send(exchange, fileData);
    }
}