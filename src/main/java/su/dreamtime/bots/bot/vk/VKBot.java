package su.dreamtime.bots.bot.vk;

import com.google.gson.JsonObject;
import com.vk.api.sdk.actions.LongPoll;
import com.vk.api.sdk.client.TransportClient;
import com.vk.api.sdk.client.VkApiClient;
import com.vk.api.sdk.client.actors.GroupActor;
import com.vk.api.sdk.client.actors.UserActor;
import com.vk.api.sdk.exceptions.ApiException;
import com.vk.api.sdk.exceptions.ClientException;
import com.vk.api.sdk.exceptions.LongPollServerKeyExpiredException;
import com.vk.api.sdk.httpclient.HttpTransportClient;
import com.vk.api.sdk.objects.callback.longpoll.responses.GetLongPollEventsResponse;
import com.vk.api.sdk.objects.groups.LongPollServer;
import com.vk.api.sdk.objects.users.UserXtrCounters;
import com.vk.api.sdk.queries.groups.GroupsGetLongPollServerQuery;
import com.vk.api.sdk.queries.longpoll.GetLongPollEventsQuery;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import su.dreamtime.bots.Client;
import su.dreamtime.bots.Main;
import su.dreamtime.bots.commands.common.Command;
import su.dreamtime.bots.util.JsonParser;
import su.dreamtime.bots.util.Util;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantLock;



public class VKBot {

    private int groupId;
    private String accessToken;
    private static Map<String, VKBot> bots = new ConcurrentHashMap<>();
    private Map<String, VkCommandClients> commands = new ConcurrentHashMap<>();
    private GroupActor actor;
    private VkApiClient vkClient;
    private String hash;
    private Set<Client> clients = Collections.synchronizedSet(new HashSet<>());

    private ScheduledExecutorService execService;
    private ScheduledFuture<?> future = null;
    private String key;
    private String server;
    private String ts;
    private ScheduledFuture<?> refreshFuture = null;
    private ReentrantLock locker = new ReentrantLock();

    private static Map<String, Level> loggerLevelMap = new HashMap<>();
    static {
        loggerLevelMap.put(HttpTransportClient.class.getName(), Level.WARN);
        loggerLevelMap.put("HttpTransportClient", Level.WARN);
        loggerLevelMap.put(TransportClient.class.getName(), Level.WARN);
        loggerLevelMap.put("TransportClient", Level.WARN);

        Logger l = LogManager.getLogger(HttpTransportClient.class);
        loggerLevelMap.forEach((s, level) -> {
            l.info(s + ": " + level.toString());
        });
    }
    private VKBot(int groupId, String accessToken) {
        this.groupId = groupId;
        this.accessToken = accessToken;
        this.hash = hashBot(groupId, accessToken);
        TransportClient client = new HttpTransportClient();
        Configurator.setLevel(loggerLevelMap);
        vkClient = new VkApiClient(client);
        actor = new GroupActor(this.groupId, this.accessToken);
        initVKCommandExecutor();
        Main.getLogger().info("VKBot with hash \"" + hash + "\" was created");
    }

    /* EXECUTOR */
    private void initVKCommandExecutor() {

        execService = Executors.newScheduledThreadPool(2);
        refreshKeys();
    }

    private void refreshKeys() {
        locker.lock();
        try {
            GroupsGetLongPollServerQuery query = vkClient.groups().getLongPollServer(actor, groupId);
            LongPollServer response = query.execute();
            key = response.getKey();
            server = response.getServer();
            ts = response.getTs();
        } catch (ApiException | ClientException e) {
            e.printStackTrace();
        }
        finally {
            if (locker.isLocked()) {
                locker.unlock();
            }
        }
    }

    private boolean stop(){
        synchronized (bots) {
            if (clients.size() > 0) {
                return false;
            }
            if (future != null)
                future.cancel(false);
            if (refreshFuture != null) {
                refreshFuture.cancel(false);
            }
            return true;
        }
    }

    public void removeClient(Client c) {
        clients.removeIf(c::equals);
        if (stop()) {
            bots.remove(hash);
            Main.getLogger().info("VKBot with hash \"" + hash + "\" was removed because of no clients");
        }
    }

    private void invoke() {
        locker.lock();
        if (vkClient == null) {
            throw new NullPointerException("API is not initialized");
        }
        try
        {
            LongPoll longPoll = new LongPoll(vkClient);
            GetLongPollEventsQuery events = longPoll.getEvents(server, key, Integer.valueOf(ts));
            GetLongPollEventsResponse eResponse = events.execute();
            List<JsonObject> objList =  eResponse.getUpdates();
            for (JsonObject obj : objList) {

                if (obj.getAsJsonPrimitive("type").getAsString().equalsIgnoreCase("message_new")) {
                    JsonObject object = obj.getAsJsonObject("object");
                    String fullText = object.getAsJsonObject("message").getAsJsonPrimitive("text").getAsString();

                    int userId = object.getAsJsonObject("message").getAsJsonPrimitive("from_id").getAsInt();
                    int chatId = object.getAsJsonObject("message").getAsJsonPrimitive("peer_id").getAsInt();
                    if(userId > 0) {

                        UserActor sender = new UserActor(userId, this.accessToken);
                        List<UserXtrCounters> counters =
                                this.vkClient.users().get(this.actor).userIds(String.valueOf(sender.getId())).execute();
                        String firstName = counters.get(0).getFirstName();
                        String lastName = counters.get(0).getLastName();
                        VkCommandData data = new VkCommandData(firstName, lastName, fullText,userId, chatId);
                        dispatchCommand(data);
                    }
                }
            }

            ts = eResponse.getTs().toString();
        } catch (ApiException | ClientException e) {
            // Если ошибка возникла из-за того, что ключ истёк
            if (e instanceof LongPollServerKeyExpiredException) {
                locker.unlock(); // снимаем лок
                refreshKeys(); // обновляем ключ
            }
        } finally {
            if (locker.isLocked()) {
                locker.unlock();
            }
        }
    }

    public void registerCommand(Client c, String command) {
        if (commands.containsKey(command)) {
            commands.get(command).addClient(c);
        } else {
            VkCommandClients data = new VkCommandClients();
            data.addClient(c);
            commands.put(command, data);
            if (future == null)
                future = execService.scheduleAtFixedRate(this::invoke, 0L, 100L, TimeUnit.MILLISECONDS);
        }
    }

    public void dispatchCommand(VkCommandData data) {
        if (commands != null) {
            commands.forEach((command, clients) -> {
                if (data.getLine().toLowerCase().startsWith(command.toLowerCase())) {
                    clients.getListeningClients().forEach(client -> {
                        client.send(new Command("EXEC_VK_COMMAND", JsonParser.toJson(data)));
                        Main.getLogger().debug("command "+data+ "was dispatched to " +client);
                    });
                }
            });
        }
    }
    /* END EXECUTOR */

    public void sendMessage(int userId, int chatId, String message) {
        if (vkClient == null) {
            return;
        }
        try {
            vkClient.messages().send(this.actor).peerId(chatId).message(message).randomId(Util.getRandom().nextInt()).execute();
        } catch (ApiException e) {
            e.printStackTrace();
        } catch (ClientException e) {
            e.printStackTrace();
        }
    }

    public Map<String, VkCommandClients> getCommands() {
        return commands;
    }

    public static Map<String, VKBot> getBots() {
        return bots;
    }

    public static VKBot create(int groupId, String accessToken, Client c) {
        synchronized (bots) {
            String hash = hashBot(groupId, accessToken);

            VKBot fromHash = getFromHash(hash);
            if (fromHash != null) {
                return fromHash;
            }
            VKBot bot = new VKBot(groupId, accessToken);
            bots.put(hash, bot);
            bot.clients.add(c);
            return bot;
        }
    }

    public static VKBot getFromHash(String hash) {
        return bots.get(hash);
    }

    public static VKBot getFromHash(int groupId, String accessToken) {
        return getFromHash(hashBot(groupId, accessToken));
    }

    public static String hashBot(int groupId, String accessToken) {
        return Util.md5(groupId + "_" + accessToken);
    }
}
