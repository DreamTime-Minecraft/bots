package su.dreamtime.bots;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import su.dreamtime.bots.bot.vk.VKBot;
import su.dreamtime.bots.bot.vk.VkCommandData;
import su.dreamtime.bots.commands.common.Command;
import su.dreamtime.bots.util.log.LoggingErrorPrintStream;
import su.dreamtime.bots.util.log.LoggingOutputStream;
import su.dreamtime.bots.util.log.LoggingPrintStream;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class Main {

    private static ServerSocket serverSocket;
    private static Logger mainLogger = LogManager.getLogger(Main.class);
    private static final Set<Client> clients = new HashSet<>();
    private static ReentrantLock runtimeLock = new ReentrantLock();
    private static boolean disconnecting = false;
    private static int port;
    public static void main(String[] args) {
        tieSystemOutAndErrToLog();

        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdown));
        setDefaults();
        try {
            initCommandLine();
            serverSocket = new ServerSocket(port);
            initConnectionChecker();
            while (true) {
                Socket clientSocket = null;
                try {
                    clientSocket = serverSocket.accept();
                    runtimeLock.lock();
                    Client client = new Client(clientSocket);
                    addClient(client);
                } catch (Exception ex) {
                    try {
                        if (disconnecting) {
                            Main.getLogger().info("disconnecting");
                            return;
                        } else {
                            Main.getLogger().warn("Error with connection.");
                        }
                        if (clientSocket != null) {
                            Main.getLogger().info("Disconnect User: " + clientSocket.getInetAddress().getHostName() + ":" + clientSocket.getPort());
                            clientSocket.close();
                        }
                    } catch (Exception ex2) {ex2.printStackTrace();}

                }
                finally {
                    if (runtimeLock.isLocked()) {
                        try {
                            runtimeLock.unlock();
                        } catch (Exception ignored) { }
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    private static void setDefaults(){
        // set project root as user.dir (For logger and properties)
        URL url = Main.class.getProtectionDomain().getCodeSource().getLocation(); //Gets the path
        String jarPath = null;
        try {
            jarPath = URLDecoder.decode(url.getFile(), "UTF-8"); //Should fix it to be read correctly by the system
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        String rootPath = new File(jarPath).getParentFile().getParentFile().getPath(); //Path of the jar

        final InputStream inputStream;
        try {
            inputStream = new FileInputStream(rootPath+ "/bots.properties");
            final Properties properties = new Properties();
            properties.load(new InputStreamReader(inputStream, "UTF-8"));
            port = Integer.parseInt(properties.getProperty("port", "44896"));
        } catch (IOException e) {
            mainLogger.warn("Cannot initialize properties");
        }
        if (port == 0) {
            mainLogger.warn("Settings default port");
            port = 44896;
        }
    }


    public static void shutdown() {
        runtimeLock.lock();
        disconnecting = true;
        System.out.println();
        getLogger().info("starting shutdown");

        try {
//            if (serverSocket!= null) {
            for (Client client : clients) {
                client.close();
            }
            clients.clear();
            serverSocket.close();
//            }
            Main.getLogger().info("Server closed");
        } catch (Exception e) {
            mainLogger.error("shutdown error: ", e);
        }

        // Сначала отключаем сервер сокет, потом ботов

        getLogger().info("shutdowned");
    }


    public static void tieSystemOutAndErrToLog() {
        System.setOut(new LoggingPrintStream(new LoggingOutputStream(
                getLogger(), Level.INFO), true));
        System.setErr(new LoggingErrorPrintStream(System.err, true));
    }

    public static Logger getLogger() {
        return mainLogger;
    }

    private static void initCommandLine() {
        Scanner scanner = new Scanner(System.in);
        Runnable runnable = () -> {
            Main.getLogger().info("input command:");
            while (true) {
                String in = scanner.nextLine();
                try {
                    runtimeLock.lock();
                    Main.getLogger().info(">"+in);
                    if (in.equalsIgnoreCase("")) {
                        Main.getLogger().info("empty command!");
                    }
                    else if (in.equalsIgnoreCase("stop")) {
                        break;
                    }
                    else {
                        for (Map.Entry<String, VKBot> entry : VKBot.getBots().entrySet()) {
                            VkCommandData data = new VkCommandData("BOT", "SERVER", in, -1, -1);
                            data.setLine(".cmd-hlwrld");
                            data.setChatId(-1);
                            data.setUserId(-1);
                            entry.getValue().dispatchCommand(data);
                        }
                    }
                }
                finally {
                    runtimeLock.unlock();
                }

            }
            System.exit(0);
        };
        new Thread(runnable).start();
    }

    public static void checkClients() {
        synchronized (clients) {
            Iterator<Client> clientIterator = clients.iterator();
            while (clientIterator.hasNext()) {
                Client client = clientIterator.next();
                if (client.getSocket().isClosed() || !client.getSocket().isConnected()) {
                    clientIterator.remove();
                    Main.getLogger().info("Client " +client.getIp() + ":" + client.getPort() + " was disconnected");
                }
            }
        }
    }

    private static void initConnectionChecker() {

        Runnable runnable = () -> {
            while (true) {
                checkClients();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        };
        new Thread(runnable).start();
    }

    public static void addClient(Client c ) {
        synchronized (clients) {
            clients.add(c);
        }
    }

    public static void sendCommandForName(String clientName, Command command) {
        synchronized (clients) {
            for (Client client : clients) {
                if (client.getName().equalsIgnoreCase(clientName)) {
                    client.send(command);
                }
            }
        }
    }

}