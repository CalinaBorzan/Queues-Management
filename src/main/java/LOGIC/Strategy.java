package LOGIC;

import MODEL.Client;
import MODEL.Server;

import java.util.List;

public interface Strategy {
    void putClients(Client client, List<Server>servers);
}
