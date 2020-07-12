package su.dreamtime.bots;

import su.dreamtime.bots.bot.vk.VKBot;
import su.dreamtime.bots.commands.common.Command;
import su.dreamtime.bots.commands.common.CommandListener;
import su.dreamtime.bots.commands.common.CommandResponse;
import su.dreamtime.bots.util.JsonParser;

import java.io.*;
import java.net.Socket;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;

public class Client extends Thread implements AutoCloseable {
    private Socket socket;
    private final String ip;
    private final int port;
    private BufferedWriter out;
    private BufferedReader in;
    private String name = "UNNAMED";
    private ReentrantLock messageLock = new ReentrantLock();
    private String vkHash;

    public Client(Socket socket) {
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostName();
        this.port = socket.getPort();
        this.vkHash = null;
        Main.getLogger().info("Connected user from " + ip + ":" + port);
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (socket.isClosed()) {
                    return;
                }
                try {
                    String str = null;
                    try {
                        str = in.readLine();

                        if (str == null) {
                            throw new NullPointerException();
                        }
                    } catch (NullPointerException e) {
                        try {
                            this.close();
                            Main.checkClients();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                    }
                    if (str == null) { // если вернул null, значит клиент отсоединился. Закрываем подключение.
                        try {
                            this.close();
                            Main.checkClients();
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        return;
                    }
                    Main.getLogger().debug("Client " + this + " send message \"" + str + "\"");

                    Command cmd = JsonParser.parseJson(str, Command.class);
                    if (cmd == null || cmd.getName() == null) {

                        out.write("\n");
                        out.flush();
                        continue;
                    }
                    CommandResponse response = CommandListener.execute(this, cmd);

                    if (cmd.response) {
                        String jsonResponse = JsonParser.toJson(response);
                        out.write(jsonResponse + "\n");
                        out.flush();
                    }
                }
                finally {
                }
            }
        } catch (IOException e) {
            if (e.getMessage().equalsIgnoreCase("socket closed")) {
                Main.getLogger().error("Cannot read. Socket was closed.");
            }
            else {
                e.printStackTrace();
            }
        }

    }

    public void send(Command cmd) {
        try {
            cmd.setResponse(false);
            String out = JsonParser.toJson(cmd);
            Main.getLogger().debug("BotClient.send(Command): " + out);
            this.out.write(out + "\n");
            this.out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }

    }

    public String getVkHash() {
        return vkHash;
    }

    public void setVkHash(String vkHash) {
        this.vkHash = vkHash;
    }

    @Override
    public void close() throws Exception {
        if (vkHash != null) {
            VKBot bot = VKBot.getFromHash(vkHash);
            if (bot != null) {
                bot.getCommands().forEach((command, data) -> {
                    data.getListeningClients().removeIf(this::equals);
                });
                bot.removeClient(this);
            }
        }
        socket.close();
    }

    public Socket getSocket() {
        return socket;
    }

    public String getIp() {
        return ip;
    }

    public int getPort() {
        return port;
    }


    public String getClientName() {
        return name;
    }

    public void setClientName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Client client = (Client) o;
        return port == client.port &&
                ip.equals(client.ip);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ip, port);
    }

    @Override
    public String toString() {
        return name + " " + ip + ":"+port ;
    }

}

