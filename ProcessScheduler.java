import javax.swing.*;
import java.awt.Dimension;
import java.awt.BorderLayout;
import java.text.DecimalFormat;
import java.util.*;
import java.awt.Font;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.BorderLayout;


class Process {
    int pid, arrivalTime, burstTime, completionTime, turnaroundTime, waitingTime, remainingTime, startTime;

    public Process(int pid, int arrivalTime, int burstTime) {
        this.pid = pid;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.remainingTime = burstTime;
    }

    public void calculateTimes() {
        turnaroundTime = completionTime - arrivalTime;
        waitingTime = turnaroundTime - burstTime;
    }
}

// Base class for Scheduling Algorithms
abstract class SchedulingAlgorithm {
    protected List<Process> processes;
    protected List<int[]> ganttChartData = new ArrayList<>(); // Store Gantt Chart Data

    public SchedulingAlgorithm(List<Process> processes) {
        this.processes = new ArrayList<>(processes);
    }

    public abstract void schedule();
    public void displayResults(String algorithmName) {
    System.out.println("\n" + algorithmName);
    System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");
    double totalWaitingTime = 0, totalTurnaroundTime = 0;

    for (Process p : processes) {
        p.calculateTimes();
        System.out.println(p.pid + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" + p.completionTime + "\t" + p.turnaroundTime + "\t" + p.waitingTime);
        
        totalWaitingTime += p.waitingTime;
        totalTurnaroundTime += p.turnaroundTime;
    }

    DecimalFormat df = new DecimalFormat("#.##");

    // Calculate and display averages
    double avgWaitingTime = totalWaitingTime / processes.size();
    double avgTurnaroundTime = totalTurnaroundTime / processes.size();
    
   System.out.println("Average Waiting Time: " + df.format(avgWaitingTime));
    System.out.println("Average Turnaround Time: " + df.format(avgTurnaroundTime));
    // Show Gantt Chart in GUI
    SwingUtilities.invokeLater(() -> new GanttChartGUI(algorithmName, ganttChartData, processes, avgWaitingTime, avgTurnaroundTime));
}


    // public void displayResults(String algorithmName) {
    //     System.out.println("\n" + algorithmName);
    //     System.out.println("PID\tAT\tBT\tCT\tTAT\tWT");
    //     for (Process p : processes) {
    //         p.calculateTimes();
    //         System.out.println(p.pid + "\t" + p.arrivalTime + "\t" + p.burstTime + "\t" + p.completionTime + "\t" + p.turnaroundTime + "\t" + p.waitingTime);
    //     }

    //     // Show Gantt Chart in GUI
    //    SwingUtilities.invokeLater(() -> new GanttChartGUI(algorithmName, ganttChartData, processes));
    // }
}

// First Come First Serve (FCFS) Scheduling
class FCFSScheduling extends SchedulingAlgorithm {
    public FCFSScheduling(List<Process> processes) {
        super(processes);
    }

    @Override
    public void schedule() {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0;

        for (Process p : processes) {
            if (currentTime < p.arrivalTime) {
                currentTime = p.arrivalTime;
            }
            p.startTime = currentTime;
            p.completionTime = currentTime + p.burstTime;
            ganttChartData.add(new int[]{p.pid, p.startTime, p.completionTime});
            currentTime = p.completionTime;
        }
        displayResults("FCFS Scheduling");
    }
}

// Shortest Job First (Non-Preemptive)
class SJFScheduling extends SchedulingAlgorithm {
    public SJFScheduling(List<Process> processes) {
        super(processes);
    }

    @Override
    public void schedule() {
        processes.sort(Comparator.comparingInt(p -> p.arrivalTime));
        int currentTime = 0, completed = 0;
        boolean[] visited = new boolean[processes.size()];

        while (completed < processes.size()) {
            int idx = -1, minBurst = Integer.MAX_VALUE;

            for (int i = 0; i < processes.size(); i++) {
                if (!visited[i] && processes.get(i).arrivalTime <= currentTime) {
                    if (processes.get(i).burstTime < minBurst) {
                        minBurst = processes.get(i).burstTime;
                        idx = i;
                    }
                }
            }

            if (idx == -1) {
                currentTime++;
            } else {
                Process p = processes.get(idx);
                visited[idx] = true;
                p.startTime = currentTime;
                p.completionTime = currentTime + p.burstTime;
                ganttChartData.add(new int[]{p.pid, p.startTime, p.completionTime});
                currentTime = p.completionTime;
                completed++;
            }
        }
        displayResults("SJF Scheduling");
    }
}
class SchedulerInputGUI extends JFrame {
    private List<JTextField[]> processFields = new ArrayList<>();
    private JComboBox<String> algorithmCombo;
    private JTextField quantumField;
    private JPanel processPanel;

    public SchedulerInputGUI() {
        setTitle("Process Scheduler Input");
        setSize(500, 400);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        JPanel topPanel = new JPanel();
        topPanel.add(new JLabel("Number of Processes:"));
        JTextField processCountField = new JTextField(5);
        topPanel.add(processCountField);
        JButton generateButton = new JButton("Generate Fields");
        topPanel.add(generateButton);

        add(topPanel, BorderLayout.NORTH);

        processPanel = new JPanel();
        processPanel.setLayout(new BoxLayout(processPanel, BoxLayout.Y_AXIS));
        add(new JScrollPane(processPanel), BorderLayout.CENTER);

        JPanel bottomPanel = new JPanel();
        algorithmCombo = new JComboBox<>(new String[]{"FCFS", "SJF", "Round Robin"});
        bottomPanel.add(new JLabel("Algorithm:"));
        bottomPanel.add(algorithmCombo);

        quantumField = new JTextField(5);
        bottomPanel.add(new JLabel("Quantum:"));
        bottomPanel.add(quantumField);

        JButton startButton = new JButton("Start Scheduling");
        bottomPanel.add(startButton);
        add(bottomPanel, BorderLayout.SOUTH);

        // Generate input fields
        generateButton.addActionListener(e -> {
            processPanel.removeAll();
            processFields.clear();
            try {
                int count = Integer.parseInt(processCountField.getText());
                for (int i = 0; i < count; i++) {
                    JPanel row = new JPanel();
                    row.add(new JLabel("P" + (i + 1) + " AT:"));
                    JTextField at = new JTextField(5);
                    row.add(at);
                    row.add(new JLabel("BT:"));
                    JTextField bt = new JTextField(5);
                    row.add(bt);
                    processFields.add(new JTextField[]{at, bt});
                    processPanel.add(row);
                }
                processPanel.revalidate();
                processPanel.repaint();
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid number!");
            }
        });

        // Start Scheduling
        startButton.addActionListener(e -> {
            List<Process> processList = new ArrayList<>();
            for (int i = 0; i < processFields.size(); i++) {
                try {
                    int at = Integer.parseInt(processFields.get(i)[0].getText());
                    int bt = Integer.parseInt(processFields.get(i)[1].getText());
                    processList.add(new Process(i + 1, at, bt));
                } catch (NumberFormatException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid input at process " + (i + 1));
                    return;
                }
            }

            String selectedAlgo = (String) algorithmCombo.getSelectedItem();
            SchedulingAlgorithm scheduler = null;

            switch (selectedAlgo) {
                case "FCFS":
                    scheduler = new FCFSScheduling(processList);
                    break;
                case "SJF":
                    scheduler = new SJFScheduling(processList);
                    break;
                case "Round Robin":
                    try {
                        int tq = Integer.parseInt(quantumField.getText());
                        scheduler = new RoundRobinScheduling(processList, tq);
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(this, "Invalid time quantum!");
                        return;
                    }
                    break;
            }

            if (scheduler != null) {
                scheduler.schedule();
            }
        });

        setVisible(true);
    }
}


// Round Robin Scheduling
class RoundRobinScheduling extends SchedulingAlgorithm {
    private int timeQuantum;
    private double avgWaitingTime; // Declare avgWaitingTime
    private double avgTurnaroundTime; // Declare avgTurnaroundTime

    public RoundRobinScheduling(List<Process> processes, int timeQuantum) {
        super(processes);
        this.timeQuantum = timeQuantum;
    }

    @Override
    public void schedule() {
        List<Process> allProcesses = new ArrayList<>(processes); // copy for iteration
        Deque<Process> queue = new ArrayDeque<>();
        int currentTime = 0;

        // Sort processes by arrival time
        allProcesses.sort(Comparator.comparingInt(p -> p.arrivalTime));

        while (!allProcesses.isEmpty() || !queue.isEmpty()) {
            // Add processes that have arrived to the queue
            Iterator<Process> it = allProcesses.iterator();
            while (it.hasNext()) {
                Process p = it.next();
                if (p.arrivalTime <= currentTime) {
                    queue.offerLast(p);
                    it.remove();
                } else {
                    break; // list is sorted, so stop checking
                }
            }

            if (queue.isEmpty()) {
                currentTime++; // wait for process to arrive
                continue;
            }

            Process p = queue.pollFirst();
            int execTime = Math.min(timeQuantum, p.remainingTime);

            updateGanttChart(p, currentTime, execTime);
            currentTime += execTime;
            p.remainingTime -= execTime;

            // Check again for newly arrived processes during execution
            it = allProcesses.iterator();
            while (it.hasNext()) {
                Process newP = it.next();
                if (newP.arrivalTime <= currentTime) {
                    queue.offerLast(newP);
                    it.remove();
                } else {
                    break;
                }
            }

            if (p.remainingTime > 0) {
                queue.offerLast(p); // still not finished
            } else {
                p.completionTime = currentTime;
            }
        }

        // Step 1: Calculate Waiting Time and Turnaround Time for each process
        double totalWaitingTime = 0;
        double totalTurnaroundTime = 0;

        for (Process p : processes) {
            p.turnaroundTime = p.completionTime - p.arrivalTime;
            p.waitingTime = p.turnaroundTime - p.burstTime;
            
            totalWaitingTime += p.waitingTime;
            totalTurnaroundTime += p.turnaroundTime;
        }

        // Step 2: Calculate average Waiting Time and Turnaround Time
        double avgWaitingTime = totalWaitingTime / processes.size();
        double avgTurnaroundTime = totalTurnaroundTime / processes.size();

        // Step 3: Call setAverageMetrics() to set the averages
        setAverageMetrics(avgWaitingTime, avgTurnaroundTime);

        // Step 4: Display the results (optional)
        displayResults("Round Robin Scheduling (TQ: " + timeQuantum + ")");
    }

    // Method to set Average Waiting Time and Turnaround Time
    private void setAverageMetrics(double avgWaitingTime, double avgTurnaroundTime) {
        this.avgWaitingTime = avgWaitingTime;
        this.avgTurnaroundTime = avgTurnaroundTime;
    }
     private void updateGanttChartWithAverages(double avgWaitingTime, double avgTurnaroundTime) {
        // Add a special entry at the end of the Gantt chart to show averages
        ganttChartData.add(new int[]{-1, -1, -1});  // Empty entry as placeholder for average

        // Add averages to Gantt chart
        ganttChartData.add(new int[]{-2, -1, -1}); // Process ID -2 for AWT display
        ganttChartData.add(new int[]{-3, -1, -1}); // Process ID -3 for ATAT display

        // Here, you would display these in your visualization (for example, in a separate row)
        System.out.println("Average Waiting Time: " + avgWaitingTime);
        System.out.println("Average Turnaround Time: " + avgTurnaroundTime);
    }

    // Method to update the Gantt Chart (as before)
    private void updateGanttChart(Process p, int start, int duration) {
        ganttChartData.add(new int[]{p.pid, start, start + duration});
    }
}


// GUI for Gantt Chart
class GanttChartGUI extends JFrame {
    private List<int[]> ganttData;
    private List<Process> processes;

    public GanttChartGUI(String title, List<int[]> ganttData, List<Process> processes  , double avgWaitingTime, double avgTurnaroundTime) {
        this.ganttData = ganttData;
        this.processes = processes;
        setTitle(title);
        setSize(700, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Add Gantt Chart Panel
        add(new GanttPanel(), BorderLayout.NORTH);

        // Add Process Table below
        String[] columnNames = {"PID", "AT", "BT", "CT", "TAT", "WT"};
        String[][] data = new String[processes.size()][6];

        for (int i = 0; i < processes.size(); i++) {
            Process p = processes.get(i);
            p.calculateTimes();
            data[i][0] = "P" + p.pid;
            data[i][1] = String.valueOf(p.arrivalTime);
            data[i][2] = String.valueOf(p.burstTime);
            data[i][3] = String.valueOf(p.completionTime);
            data[i][4] = String.valueOf(p.turnaroundTime);
            data[i][5] = String.valueOf(p.waitingTime);
        }

        JTable table = new JTable(data, columnNames);
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

       JPanel avgPanel = new JPanel();
    avgPanel.setLayout(new BoxLayout(avgPanel, BoxLayout.Y_AXIS)); // Use vertical box layout

    // Set border and padding for the panel
    avgPanel.setBorder(BorderFactory.createTitledBorder("Average Times"));
    avgPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

    DecimalFormat df = new DecimalFormat("#.##");
    JLabel avgWTLabel = new JLabel("Average Waiting Time: " + df.format(avgWaitingTime) + " ms");
    JLabel avgTATLabel = new JLabel("Average Turnaround Time: " + df.format(avgTurnaroundTime) + " ms");

    avgWTLabel.setFont(new Font("Arial", Font.BOLD, 14));
    avgTATLabel.setFont(new Font("Arial", Font.BOLD, 14));

    avgPanel.add(avgWTLabel);
    avgPanel.add(avgTATLabel);

    add(avgPanel, BorderLayout.SOUTH);

        setVisible(true);
    }

    // Inner class for Gantt Chart drawing
    class GanttPanel extends JPanel {
    private Map<Integer, Color> processColors = new HashMap<>();

    public GanttPanel() {
        generateProcessColors();
    }

    private void generateProcessColors() {
        Random rand = new Random();
        for (int[] data : ganttData) {
            int pid = data[0];
            if (!processColors.containsKey(pid)) {
                // Generate bright, unique colors
                Color color = new Color(rand.nextInt(128) + 100, rand.nextInt(128) + 100, rand.nextInt(128) + 100);
                processColors.put(pid, color);
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int x = 50, y = 40, height = 30;

        int totalTime = ganttData.get(ganttData.size() - 1)[2];
        int scale = 600 / totalTime;

        for (int[] data : ganttData) {
            int pid = data[0];
            int start = data[1];
            int end = data[2];
            int duration = end - start;
            int width = duration * scale;

            g.setColor(processColors.get(pid));
            g.fillRect(x, y, width, height);
            g.setColor(Color.BLACK);
            g.drawRect(x, y, width, height);
            g.drawString("P" + pid, x + width / 2 - 10, y + 20);
            g.drawString(String.valueOf(start), x, y + 50);
            x += width;
            g.drawString(String.valueOf(end), x, y + 50);
        }
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(700, 100);
    }
}

    
}


// Main Program to Run Scheduling Algorithms
public class ProcessScheduler {
    public static void main(String[] args) {
         SwingUtilities.invokeLater(() -> new SchedulerInputGUI());
        // Scanner sc = new Scanner(System.in);
        // List<Process> processes = new ArrayList<>();

        // System.out.print("Enter number of processes: ");
        // int n = sc.nextInt();

        // for (int i = 0; i < n; i++) {
        //     System.out.print("Enter Arrival Time and Burst Time for Process " + (i + 1) + ": ");
        //     int at = sc.nextInt();
        //     int bt = sc.nextInt();
        //     processes.add(new Process(i + 1, at, bt));
        // }

        // System.out.println("\nChoose Scheduling Algorithm:");
        // System.out.println("1. FCFS (First Come First Serve)");
        // System.out.println("2. SJF (Shortest Job First Non-Preemptive)");
        // System.out.println("3. Round Robin");
        // System.out.print("Enter your choice: ");
        // int choice = sc.nextInt();

        // SchedulingAlgorithm scheduler = null;

        // switch (choice) {
        //     case 1:
        //         scheduler = new FCFSScheduling(processes);
        //         break;
        //     case 2:
        //         scheduler = new SJFScheduling(processes);
        //         break;
        //     case 3:
        //         System.out.print("Enter Time Quantum: ");
        //         int tq = sc.nextInt();
        //         scheduler = new RoundRobinScheduling(processes, tq);
        //         break;
        //     default:
        //         System.out.println("Invalid choice!");
        //         return;
        // }

        // scheduler.schedule();
        // sc.close();
    }
}
