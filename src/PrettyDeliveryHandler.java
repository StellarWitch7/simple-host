import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;

public class PrettyDeliveryHandler extends RequestHandler {
    @Override
    protected void logic(HttpServerExchange exchange) throws Exception {
        String filepath = exchange.getRelativePath();
        String filename = filepath.substring(filepath.lastIndexOf("/") + 1);
        String mimeType = getMimeType(filename);
        String output = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <title>Pretty " + filename + "</title>\n" +
                "    <style>\n" +
                "        embed {\n" +
                "            text-align: center;\n" +
                "            position: absolute;\n" +
                "            inset: 0;\n" +
                "            margin: auto;\n" +
                "            width: 85%;\n" +
                "            height: 85%;\n" +
                "        }\n" +
                "    </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "    <embed type=\"" + mimeType + "\" src=\"" + filepath + "\">\n" +
                "</body>\n" +
                "</html>";
        exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/html");
        send(exchange, output);
    }
}
