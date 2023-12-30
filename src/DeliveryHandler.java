import java.io.*;
import java.net.Socket;
import java.util.Date;

public class DeliveryHandler extends Thread {
    static final String nl = "\r\n";
    private final Socket connection;
    private final String path;

    public DeliveryHandler(Socket connection, String path) {
        this.connection = connection;
        this.path = path;
    }

    @Override
    public void run() {
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            OutputStream out = new BufferedOutputStream(connection.getOutputStream());
            PrintStream pout = new PrintStream(out, true);

            // read first line of request
            String request = in.readLine();
            if (request == null) return;

            // we ignore the rest
            while (true) {
                String ignore = in.readLine();
                if (ignore == null || ignore.isEmpty()) break;
            }

            if (!request.startsWith("GET ") ||
                    !(request.endsWith(" HTTP/1.0") || request.endsWith(" HTTP/1.1"))) {
                // bad request
                pout.print("HTTP/1.0 400 Bad Request"+ nl + nl);
            } else {
                FileInputStream file = new FileInputStream(path);
                DataInputStream dataInputStream = new DataInputStream(file);
                byte[] response = new byte[file.available()];
                dataInputStream.readFully(response);
                String filename = path.substring(path.lastIndexOf("/") + 1);

                pout.print("HTTP/1.0 200 OK" + nl +
                        "Content-Type: application/octet-stream" + nl +
                        "Content-Disposition: attachment; filename=" + filename + nl +
                        "Date: " + new Date() + nl +
                        "Content-length: " + response.length + nl + nl);
                out.write(response);
            }

            pout.close();
        } catch (Throwable tri) {
            System.err.println("Error handling request: " + tri);
        }
    }
}
