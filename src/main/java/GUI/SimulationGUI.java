package GUI;

import LOGIC.ShortestQueueStrategy;
import LOGIC.SimulationManager;
import LOGIC.Strategy;
import LOGIC.ShortestTimeStrategy;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

public class SimulationGUI extends JFrame {
    private JTextField clientsField;
    private JTextField queuesField;
    private JTextField maxArrivalTimeField;
    private JTextField minArrivalTimeField;
    private JTextField maxServiceTimeField;
    private JTextField minServiceTimeField;
    private JTextField simulationTimeField;
    private JComboBox<String> strategySelector;
    private JTextArea logArea;
    private JButton startButton;
    private Queues queueVisualizer;

    public SimulationGUI() {
        super("Queue Simulation");
        initializeUI();
    }

    private void initializeUI() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(500, 400);
        setLayout(new BorderLayout(5, 5));

        JPanel inputPanel = new JPanel(new GridLayout(8, 2, 5, 5));
        setBackground(Color.DARK_GRAY);
        clientsField = new JTextField("4"); queuesField = new JTextField("2");
        maxArrivalTimeField = new JTextField("30"); minArrivalTimeField = new JTextField("2");
        maxServiceTimeField = new JTextField("4"); minServiceTimeField = new JTextField("2");
        simulationTimeField = new JTextField("60");
        strategySelector = new JComboBox<>(new String[]{"Shortest Queue", "Shortest Waiting Time"});
        inputPanel.add(new JLabel("Strategy:"));
        inputPanel.add(strategySelector);
        String[] labels = {"Number of Clients (N):", "Number of Queues (Q):", "Min Arrival Time:", "Max Arrival Time:",
                "Min Service Time:", "Max Service Time:", "Simulation Time (tMAX):"};
        JTextField[] fields = {clientsField, queuesField, minArrivalTimeField, maxArrivalTimeField,
                minServiceTimeField, maxServiceTimeField, simulationTimeField};

        for (int i = 0; i < labels.length; i++) {
            inputPanel.add(new JLabel(labels[i]));
            inputPanel.add(fields[i]);
            fields[i].setBackground(Color.gray);
            fields[i].setBorder(BorderFactory.createLineBorder(Color.black));
        }


        startButton = new JButton("Start Simulation");
        startButton.setBackground(Color.DARK_GRAY);
        startButton.addActionListener(this::startSimulation);

        add(inputPanel, BorderLayout.NORTH);
        add(startButton, BorderLayout.SOUTH);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void startSimulation(ActionEvent event) {
        int strategyIndex = strategySelector.getSelectedIndex();
        Strategy strategy = (strategyIndex == 0) ? new ShortestQueueStrategy() : new ShortestTimeStrategy();

        int numClients = Integer.parseInt(clientsField.getText());
        int numQueues = Integer.parseInt(queuesField.getText());
        int maxArrival = Integer.parseInt(maxArrivalTimeField.getText());
        int minArrival = Integer.parseInt(minArrivalTimeField.getText());
        int maxService = Integer.parseInt(maxServiceTimeField.getText());
        int minService = Integer.parseInt(minServiceTimeField.getText());
        int simTime = Integer.parseInt(simulationTimeField.getText());

        if (queueVisualizer != null) {
            queueVisualizer.dispose();
        }
        queueVisualizer = new Queues(numQueues);
        SimulationManager manager = new SimulationManager(numQueues, logArea, simTime, numClients, maxArrival, minArrival, maxService, minService);
        manager.setQueueVisualizer(queueVisualizer);
        manager.setStrategy(strategy);
        new Thread(() -> {
            manager.startSimulation();
            double averageWaitingTime = manager.getAverageWaitingTime();
            double averageServingTime = manager.getAverageServingTime();
            int peakhour=manager.getPeakHour();
            queueVisualizer.displayFinalMetrics(averageWaitingTime, averageServingTime,peakhour);
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SimulationGUI::new);
    }
}
