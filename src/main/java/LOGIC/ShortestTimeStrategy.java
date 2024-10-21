package LOGIC;

import MODEL.Client;
import MODEL.Server;

import java.util.List;

public class ShortestTimeStrategy implements Strategy {
    public void putClients(Client client, List<Server> servers) {
        Server mostEfficientServer = null;
        int shortestTime = Integer.MAX_VALUE;
        if (servers.isEmpty()) {
            throw new IllegalStateException("No servers available");
        }

        for (Server server : servers) {
            int time = server.getWaitingTime();
            if (time < shortestTime) {
                shortestTime = time;
                mostEfficientServer = server;
            }
        }
        if (mostEfficientServer != null) {
            mostEfficientServer.addClient(client);
        }

    }
}