package GUI;

import MODEL.Client;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class Queues extends JFrame {
    private final List<JPanel> queuePanels = new ArrayList<>();
    private final JLabel timerLabel;
    private final JTextArea waitingClientsTextArea;
    private JLabel finalMetricsLabel;

    public Queues(int numQueues) {
        super("Queue Visualization");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(400, 600);
        setLayout(new BorderLayout());

        timerLabel = new JLabel("Time: 0", JLabel.CENTER);
        timerLabel.setFont(new Font("Serif", Font.BOLD, 20));
        add(timerLabel, BorderLayout.NORTH);

        JPanel queuesContainer = new JPanel();
        queuesContainer.setLayout(new GridLayout(numQueues, 1));
        for (int i = 0; i < numQueues; i++) {
            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            panel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 2));
            panel.setBorder(BorderFactory.createTitledBorder("Queue " + (i + 1)));
            queuePanels.add(panel);
            queuesContainer.add(panel);
        }

        JScrollPane scrollPane = new JScrollPane(queuesContainer);
        add(scrollPane, BorderLayout.CENTER);

        waitingClientsTextArea = new JTextArea(5, 20);
        waitingClientsTextArea.setEditable(false);
        JScrollPane textAreaScrollPane = new JScrollPane(waitingClientsTextArea);
        add(textAreaScrollPane, BorderLayout.SOUTH);

        finalMetricsLabel = new JLabel("", JLabel.CENTER);
        add(finalMetricsLabel, BorderLayout.EAST);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void updateQueue(int queueIndex, List<Client> clients, int currentTime) {
        timerLabel.setText("Time: " + currentTime);

        JPanel panel = queuePanels.get(queueIndex);
        panel.removeAll();

        for (Client client : clients) {
            String clientDetails = String.format("Client %d: Arrived at %d, Serving time: %d",
                    client.getId(), client.getArrivalTime(), client.getServiceTime());
            JLabel label = new JLabel(clientDetails);
            label.setFont(new Font("Arial", Font.PLAIN, 14));
            panel.add(label);
        }

        panel.revalidate();
        panel.repaint();
    }

    public void displayFinalMetrics(double averageWaitingTime, double averageServingTime, int peakhour) {
        String metrics = String.format("Average Waiting Time: %.2f | Average Serving Time: %.2f | Peak Hour: %d", averageWaitingTime, averageServingTime, peakhour);
        finalMetricsLabel.setText(metrics);
    }

    
    public void updateWaitingClients(List<Client> clients) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("Waiting Clients:\n");
        for (Client client : clients) {
            String clientDetails = String.format("Client %d: Arrived at %d, Service Time: %d\n",
                    client.getId(), client.getArrivalTime(), client.getServiceTime());
            stringBuilder.append(clientDetails);
        }
        waitingClientsTextArea.setText(stringBuilder.toString());
    }

}
