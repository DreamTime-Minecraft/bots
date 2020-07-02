package su.dreamtime.bots.bot.vk;

import su.dreamtime.bots.Client;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class VkCommandClients {
    private Set<Client> listeningClients;

    public VkCommandClients() {
        listeningClients = Collections.synchronizedSet(new HashSet<>());
    }

    public Set<Client> getListeningClients() {
        return listeningClients;
    }

    public void addClient(Client c) {
        listeningClients.add(c);
    }
}
