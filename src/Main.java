import io.undertow.Handlers;
import io.undertow.Undertow;

import java.io.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.System.exit;

class Main
{
    public static final String alotofspaces = "                                                                       ";
    public static AtomicInteger processesRunning = new AtomicInteger(0);
    public static int port = 63063;
    public static String hostIP;
    public static String rootPath;
    public static String logFilePath;
    public static File pid;
    public static File logFile;
    public static PrintStream log;
    public static boolean isClosing = false;

    static void init(String hostIP, int port, String rootPath) throws Exception {
        Main.hostIP = hostIP;
        Main.port = port;
        Main.rootPath = rootPath;
        Main.logFilePath = rootPath + "/log";
        File logFile = new File(logFilePath);
        if (!logFile.renameTo(new File(logFilePath + ".bak"))) throw new IOException("Could not backup log file.");
        Main.logFile = logFile;
        Main.log = new PrintStream(logFile);
        File pidFile = new File("/var/tmp/simple-host-pids/" + port);
        if (pidFile.exists()) throw new Exception("Another instance is already using port " + port + ".");
        Main.pid = pidFile;
        PrintStream pidFileWriter = new PrintStream(pidFile);;
        String pid = new File("/proc/self").getCanonicalFile().getName();
        pidFile.deleteOnExit();
        pidFileWriter.print(pid);
        pidFileWriter.flush();
        System.setErr(Main.log);
    }

    public static void main(String[] args) throws Exception {
        try {
            init(args[0], Integer.parseInt(args[1]), args[2]);
            System.out.println("Starting Undertow...");
            Undertow server = Undertow.builder()
                    .addHttpListener(port, hostIP)
                    .setHandler(Handlers.path()
                            .addPrefixPath("/download", new DeliveryHandler(true))
                            .addPrefixPath("/", new DeliveryHandler()))
                    .build();
            server.start();
            System.out.println("Started server on port " + port);
            log.println("Server running on port " + port);

            while (true) {
                System.out.println("\nEnter 'help' to list commands");
                System.out.print("|>> ");
                StringBuilder command = new StringBuilder();
                int ch;

                while ((ch = System.in.read()) != '\n') {
                    if (ch != -1) command.append((char) ch);
                }

                processCommand(command.toString());
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void processCommand(String command) {
        switch (command) {
            case "help":
                System.out.println("help -> shows this page");
                System.out.println("close -> terminates the server");
                break;
            case "close":
                close();
                break;
            default:
                break;
        }
    }

    public static void close() {
        isClosing = true;
        int timeWaited = 0;

        while (true) {
            if (processesRunning.get() == 0) {
                System.out.println("No processes running, shutting down..." + alotofspaces);
                exit(0);
            }

            System.out.print("Processes are running, finishing up " + processesRunning.get() + " processe(s)... " +
                    "(" + timeWaited + "s)\r");
            timeWaited++;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}