package LOGIC;

import MODEL.Client;
import MODEL.Server;

import java.util.List;

public class ShortestQueueStrategy implements Strategy {
    public void putClients(Client client, List<Server> servers) {
        Server mostEfficientServer = servers.get(0);
        for (Server server : servers) {
            if (server.getQueueSize() < mostEfficientServer.getQueueSize()) {
                mostEfficientServer = server;
            }
        }
        mostEfficientServer.addClient(client);
    }

}