package com.jobhunter.pages.refreshDb;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;
import com.jobhunter.LinksScraper.*;
import com.jobhunter.DataScraper.*;
import com.jobhunter.Cleaner.CleanLLM;
import com.jobhunter.database.InsertJson;

public class RefreshDbPage {
    private JFrame frame;
    private JTextArea logArea;
    private JButton startButton;
    private JButton backButton;
    private Map<String, JCheckBox> siteCheckboxes;
    private JProgressBar progressBar;

    public RefreshDbPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Refresh Database");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Top Panel with Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            // TODO: Navigate back to main page
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("Database Refresh Control", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(250)); // spacing
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Site Selection Panel
        JPanel sitePanel = new JPanel(new GridLayout(0, 2, 5, 5));
        sitePanel.setBorder(BorderFactory.createTitledBorder("Select Sites to Scrape"));
        
        siteCheckboxes = new HashMap<>();
        String[] sites = {"Rekrute", "Anapec", "EmploiMa", "KhdmaMa", 
                         "MarocAnnonces", "MonCallCenter", "StagairesMa"};
        
        for (String site : sites) {
            JCheckBox checkbox = new JCheckBox(site);
            checkbox.setSelected(true);
            siteCheckboxes.put(site, checkbox);
            sitePanel.add(checkbox);
        }

        // Control Panel
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        startButton = new JButton("Start Refresh");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startRefreshProcess());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        controlPanel.add(sitePanel, BorderLayout.CENTER);
        controlPanel.add(startButton, BorderLayout.SOUTH);

        // Log Panel
        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.setBorder(BorderFactory.createTitledBorder("Process Log"));
        
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(logArea);
        scrollPane.setPreferredSize(new Dimension(400, 200));
        logPanel.add(scrollPane, BorderLayout.CENTER);

        mainPanel.add(controlPanel, BorderLayout.NORTH);
        mainPanel.add(progressBar, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);
    }

    private void startRefreshProcess() {
        startButton.setEnabled(false);
        progressBar.setValue(0);
        logArea.setText("");
        
        // Create a background thread for the refresh process
        new Thread(() -> {
            try {
                // Step 1: Links Scraping
                updateLog("Starting links scraping process...");
                progressBar.setValue(10);
                
                for (Map.Entry<String, JCheckBox> entry : siteCheckboxes.entrySet()) {
                    if (entry.getValue().isSelected()) {
                        updateLog("Scraping links from " + entry.getKey() + "...");
                        // TODO: Call appropriate scraper based on site name
                    }
                }
                
                // Step 2: Data Scraping
                updateLog("\nStarting data scraping process...");
                progressBar.setValue(40);
                // TODO: Implement data scraping for selected sites
                
                // Step 3: Data Cleaning
                updateLog("\nCleaning scraped data...");
                progressBar.setValue(70);
                // TODO: Call CleanLLM
                
                // Step 4: Database Update
                updateLog("\nUpdating database...");
                progressBar.setValue(90);
                // TODO: Call InsertJson
                
                progressBar.setValue(100);
                updateLog("\nDatabase refresh completed successfully!");
                
            } catch (Exception e) {
                updateLog("\nError during refresh: " + e.getMessage());
                e.printStackTrace();
            } finally {
                startButton.setEnabled(true);
            }
        }).start();
    }

    private void updateLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    public void show() {
        frame.setVisible(true);
    }
}
