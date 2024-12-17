package com.jobhunter.pages.refreshDb;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import com.jobhunter.pages.refreshDb.models.ScrapingSite;
import com.jobhunter.pages.refreshDb.services.ScrapingService;
import com.jobhunter.pages.main.MainPage;

public class RefreshDbPage {
    private JFrame frame;
    private JTextArea logArea;
    private JButton startButton;
    private JButton backButton;
    private List<JCheckBox> siteCheckboxes;
    private JProgressBar progressBar;
    private final ScrapingService scrapingService;

    public RefreshDbPage() {
        siteCheckboxes = new ArrayList<>();
        scrapingService = new ScrapingService(
            this::updateLog,  // Log callback
            this::updateProgress  // Progress callback
        );
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
            MainPage.getInstance().show();
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
        
        String[] sites = {"Rekrute", "Anapec", "EmploiMa", "KhdmaMa", 
                         "MarocAnnonces", "MonCallCenter", "StagairesMa"};
        
        for (String site : sites) {
            JCheckBox checkbox = new JCheckBox(site);
            checkbox.setSelected(true);
            siteCheckboxes.add(checkbox);
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
        
        // Create a list of selected sites
        List<ScrapingSite> selectedSites = new ArrayList<>();
        for (JCheckBox checkbox : siteCheckboxes) {
            selectedSites.add(new ScrapingSite(checkbox.getText(), checkbox.isSelected()));
        }
        
        // Create a background thread for the refresh process
        new Thread(() -> {
            try {
                scrapingService.startScraping(selectedSites);
            } catch (Exception e) {
                SwingUtilities.invokeLater(() -> {
                    JOptionPane.showMessageDialog(frame,
                        "Error during refresh: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                });
                e.printStackTrace();
            } finally {
                SwingUtilities.invokeLater(() -> startButton.setEnabled(true));
            }
        }).start();
    }

    private void updateLog(String message) {
        SwingUtilities.invokeLater(() -> {
            logArea.append(message + "\n");
            logArea.setCaretPosition(logArea.getDocument().getLength());
        });
    }

    private void updateProgress(int value) {
        SwingUtilities.invokeLater(() -> progressBar.setValue(value));
    }

    public void show() {
        frame.setVisible(true);
    }
}
