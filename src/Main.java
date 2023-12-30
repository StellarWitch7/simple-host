import java.net.*;
import java.io.*;
import java.util.*;

class Main
{
    static final int port = 63063;

    public static void main(String[] args) {
        System.out.println("Started server on default port 63063");

        try {
            ServerSocket socket = new ServerSocket(port);

            while (true) {
                Socket connection = socket.accept();
                DeliveryHandler handler = new DeliveryHandler(connection, args[0]);
                handler.start();
            }
        } catch (Throwable tr) {
            System.err.println("Could not start server: " + tr);
        }
    }
}