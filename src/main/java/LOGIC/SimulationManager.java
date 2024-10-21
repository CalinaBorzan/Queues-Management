package LOGIC;

import GUI.Queues;
import MODEL.Client;
import MODEL.Server;

import javax.swing.*;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.*;

public class SimulationManager  {

    public static final String LOG_FILE = "simulation_log.txt";
    private int maxQueueSize = 0;
    private List<Client>clientsGenerated;
    private double averageWaitingTime;
    private double averageServingTime;
    private int peakHour;
    private List<Future<Integer>> futures;
    public int MAX_TIME;
    private final int numberOfServers;
    private final JTextArea logArea;
    private ExecutorService executor;
    private List<Server> servers;
    private Queue<Client> clients;
    private Strategy strategy;
    public int numberOfClients;
    public int maxProcessingTime;
    public int minProcessingTime;
    public int maxArrivalTime;
    public int minArrivalTime;
    private Queues queueVisualizer;

    public SimulationManager(int numberOfServers, JTextArea logArea, int maxTime, int numClients, int maxArr, int minArr, int maxProc, int minProc) {
        this.numberOfServers = numberOfServers;
        this.logArea = logArea;
        this.MAX_TIME = maxTime;
        this.numberOfClients = numClients;
        this.maxArrivalTime = maxArr;
        this.minArrivalTime = minArr;
        this.maxProcessingTime = maxProc;
        this.minProcessingTime = minProc;
        this.clients = new LinkedList<>();
        this.servers = new ArrayList<>();
        this.executor = Executors.newFixedThreadPool(numberOfServers);
        this.futures = new ArrayList<>();

    }
    private void init() {

        for (int i = 0; i < numberOfServers; i++) {
            Server server = new Server();
            servers.add(server);
            futures.add(executor.submit(server));
        }

      generateRandomClients(numberOfClients,maxProcessingTime,minProcessingTime,maxArrivalTime,minArrivalTime);

    }
    public void setStrategy(Strategy strategy)
    {this.strategy=strategy;

    }
    public void startSimulation() {
        init();
        int currentTime = 0;
        while (currentTime < MAX_TIME) {
            distributeClients(currentTime);
            updateVisualizer(currentTime);
            logState(currentTime);
            recordQueueSizes(currentTime);
            currentTime++;
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        cleanUp();
        calculateAverages();
    }

    private void logState(int currentTime) {
        String file="simulation_log.txt";
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(file, true)))) {
         out.println("Time " + currentTime);
        if (clients.isEmpty()) {
            out.println("Waiting clients: none");
        } else {
            clients.forEach(client ->
                    out.printf("(%d,%d,%d); ", client.getId(), client.getArrivalTime(), client.getServiceTime()));
        }
        out.println();
        for (int i = 0; i < servers.size(); i++) {
            out.print("Queue " + (i + 1) + ": ");
            List<Client> serverClients = servers.get(i).getClients();
            if (serverClients.isEmpty()) {
                out.println("closed");
            } else {
                serverClients.forEach(client ->
                        out.printf("(%d,%d,%d); ", client.getId(), client.getArrivalTime(), client.getServiceTime()));
                out.println();
            }
        }
    } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (queueVisualizer != null) {
            queueVisualizer.updateWaitingClients(new ArrayList<>(clients));
        }
    }
    private void recordQueueSizes(int currentTime) {
        int currentQueueSize = servers.stream().mapToInt(Server::getQueueSize).sum();
        if (currentQueueSize > maxQueueSize) {
            maxQueueSize = currentQueueSize;
            peakHour = currentTime;
        }
    }
    private void distributeClients(int currentTime) {
        Iterator<Client> iterator = clients.iterator();
        while (iterator.hasNext()) {
            Client client = iterator.next();
            if (client.getArrivalTime() == currentTime) {
                strategy.putClients(client, servers);
                iterator.remove();
            }
        }
    }

    private void generateRandomClients(int numberOfClients,int maxProcessingTime,int minProcessingTime,int maxArrivalTime,int minArrivalTime)
    {
        Random rand=new Random();
        clientsGenerated=new ArrayList<>();
        for(int i=0;i<numberOfClients;i++)
        {
            int processingTime=minProcessingTime+rand.nextInt(maxProcessingTime-minProcessingTime+1);
            int arrivalTime=minArrivalTime+rand.nextInt(maxArrivalTime-minArrivalTime+1);
            clientsGenerated.add(new Client(i+1,arrivalTime,processingTime));
        }
        Collections.sort(clientsGenerated,Comparator.comparingInt(Client::getArrivalTime));
        clients.addAll(clientsGenerated);
    }

    private void cleanUp() {

        for (Server server : servers) {
            server.endSimulation();
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }

        for (Future<Integer> future : futures) {
            try {
                Integer waitingTime = future.get();
                System.out.println("Queque's time" + waitingTime);
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Error retrieving server results: " + e.getMessage());
            }
        }
    }
    private void calculateAverages() {

        int totalWaitingTime = clientsGenerated.stream().mapToInt(Client::getWaitingTime).sum();
         averageWaitingTime = (double) totalWaitingTime /clientsGenerated.size();
        int totalClientsProcessed = servers.stream().mapToInt(Server::getClientsProcessed).sum();
        int totalServiceTime = servers.stream().mapToInt(server -> server.getTotalServiceTime().get()).sum();
         averageServingTime = totalClientsProcessed > 0 ? (double) totalServiceTime / totalClientsProcessed : 0.0;
        try (PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(LOG_FILE, true)))) {
           out.printf("Average Waiting Time: %.2f\n",averageWaitingTime);
            out.printf("Average Serving Time: %.2f\n",averageServingTime);
            if (peakHour == -1) {
                out.println("Peak Hour: No peak hour as no clients were processed");
            } else {
                out.printf("Peak Hour: %d\n", peakHour);
            }
        } catch (IOException e) {
            System.err.println("Error writing final metrics to log file: " + e.getMessage());
        }
        if (queueVisualizer != null) {
            queueVisualizer.displayFinalMetrics(averageWaitingTime, averageServingTime,peakHour);
        }
    }
    public double getAverageWaitingTime() {
        return averageWaitingTime;
    }

    public double getAverageServingTime() {
        return averageServingTime;
    }

    public int getPeakHour() {
        return peakHour;
    }
    public void setQueueVisualizer(Queues queueVisualizer) {
        this.queueVisualizer = queueVisualizer;
    }

    private void updateVisualizer(int currentTime) {
        if (queueVisualizer == null) return;
        for (int i = 0; i < servers.size(); i++) {
            List<Client> clientsCopy = new ArrayList<>(servers.get(i).getClients());
            int finalI = i;
            SwingUtilities.invokeLater(() -> queueVisualizer.updateQueue(finalI, clientsCopy, currentTime));
        }
    }
}
