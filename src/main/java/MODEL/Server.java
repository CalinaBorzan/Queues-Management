package MODEL;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Callable<Integer> {
    private BlockingQueue<Client> clients;
    private AtomicInteger waitingPeriod;
    private volatile boolean endSim;
    private AtomicInteger totalServiceTime;
    private int clientsProcessed;

    public Server() {
        this.clients = new LinkedBlockingDeque<>();
        this.waitingPeriod = new AtomicInteger(0);
        this.totalServiceTime=new AtomicInteger(0);
        this.clientsProcessed=0;
        this.endSim = false;

    }

    public void addClient(Client client) {
        if (!client.isServiceStarted()) {
            client.setWaitingTime(this.waitingPeriod.get());
            client.setServiceStarted(true);
            System.out.println("Server - Adding client ID " + client.getId() + " with waiting time: " + client.getWaitingTime());
        }
        clients.add(client);
        waitingPeriod.addAndGet(client.getServiceTime());
    }

    @Override
    public Integer call() {
        try {
            while (!endSim) {
                if (!clients.isEmpty()) {
                    Client client = clients.peek();
                    if (client != null) {
                        client.decrementServiceTime();
                        waitingPeriod.decrementAndGet();
                        if (client.getServiceTime() == 0) {
                            clients.poll();
                            totalServiceTime.addAndGet(client.getInitialServiceTime());
                            clientsProcessed++;
                            System.out.println("Server - Client ID " + client.getId() + " completed. Adjusted waiting period: " + waitingPeriod.get());
                        }
                    }
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        return waitingPeriod.get();
    }


    public List<Client> getClients() {
        return new ArrayList<>(clients);
    }

    public int getWaitingTime() {
        return waitingPeriod.get();
    }
    public int getQueueSize()
    {
        return clients.size();
    }

    public void endSimulation() {
        endSim=true;
        while (!clients.isEmpty()) {
            Client client = clients.poll();
            if (client.getServiceTime() == 0) {
                int actualServiceTime = client.getInitialServiceTime() - client.getServiceTime();
                totalServiceTime.addAndGet(actualServiceTime);
                clientsProcessed++;
            }
        }

    }
    public int getClientsProcessed() {
        return  clientsProcessed;
    }

    public AtomicInteger getTotalServiceTime() {
        return totalServiceTime;
    }
}

