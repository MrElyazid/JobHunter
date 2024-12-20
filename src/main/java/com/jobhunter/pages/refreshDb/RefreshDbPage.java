package com.jobhunter.pages.refreshDb;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import com.jobhunter.pages.refreshDb.models.ScrapingSite;
import com.jobhunter.pages.refreshDb.services.ScrapingService;
import com.jobhunter.pages.main.MainPage;

public class RefreshDbPage {
    private JFrame frame;
    private JTextArea logArea;
    private JButton startButton;
    private JButton backButton;
    private JButton exportButton;
    private List<ScrapingSite> scrapingSites;
    private Map<String, JSpinner> pageCountSpinners;
    private JProgressBar progressBar;
    private final ScrapingService scrapingService;
    
    // Radio buttons for cleaning pipeline selection
    private JRadioButton regexCleanerRadio;
    private JRadioButton llmCleanerRadio;
    
    // Radio buttons for database operation mode
    private JRadioButton appendModeRadio;
    private JRadioButton replaceModeRadio;

    public RefreshDbPage() {
        scrapingSites = new ArrayList<>();
        pageCountSpinners = new HashMap<>();
        scrapingService = new ScrapingService(
            this::updateLog,  // Log callback
            this::updateProgress  // Progress callback
        );
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Refresh Database");
        frame.setBounds(100, 100, 1200, 800);
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
        topPanel.add(Box.createHorizontalStrut(350));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Main Content Panel
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Left Panel for Site Selection
        JPanel leftPanel = new JPanel(new BorderLayout(5, 5));
        
        // Site Selection Panel with Page Count
        JPanel sitePanel = new JPanel(new GridBagLayout());
        sitePanel.setBorder(BorderFactory.createTitledBorder("Configure Sites to Scrape"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Headers
        gbc.gridx = 0;
        sitePanel.add(new JLabel("Site"), gbc);
        gbc.gridx = 1;
        sitePanel.add(new JLabel("Enable"), gbc);
        gbc.gridx = 2;
        sitePanel.add(new JLabel("Pages"), gbc);
        
        String[] sites = {"Rekrute", "Anapec", "EmploiMa", "KhdmaMa", 
                         "MarocAnnonces", "MonCallCenter", "StagairesMa"};
        
        for (int i = 0; i < sites.length; i++) {
            gbc.gridy = i + 1;
            
            // Site name
            gbc.gridx = 0;
            sitePanel.add(new JLabel(sites[i]), gbc);
            
            // Checkbox
            gbc.gridx = 1;
            JCheckBox checkbox = new JCheckBox();
            checkbox.setSelected(true);
            sitePanel.add(checkbox, gbc);
            
            // Page count spinner
            gbc.gridx = 2;
            SpinnerNumberModel spinnerModel = new SpinnerNumberModel(5, 1, 50, 1);
            JSpinner pageSpinner = new JSpinner(spinnerModel);
            pageCountSpinners.put(sites[i], pageSpinner);
            sitePanel.add(pageSpinner, gbc);
            
            // Create and store ScrapingSite object
            ScrapingSite site = new ScrapingSite(sites[i], true);
            scrapingSites.add(site);
            
            // Add listeners
            checkbox.addActionListener(e -> {
                site.setSelected(checkbox.isSelected());
                pageSpinner.setEnabled(checkbox.isSelected());
            });
            
            pageSpinner.addChangeListener(e -> 
                site.setPageCount((Integer) pageSpinner.getValue())
            );
        }
        
        leftPanel.add(sitePanel, BorderLayout.CENTER);

        // Right Panel for Settings
        JPanel rightPanel = new JPanel(new GridLayout(3, 1, 5, 5));
        
        // Cleaning Pipeline Selection Panel
        JPanel cleanerPanel = new JPanel(new GridLayout(0, 1, 2, 2));
        cleanerPanel.setBorder(BorderFactory.createTitledBorder("Select Cleaning Pipeline"));
        
        regexCleanerRadio = new JRadioButton("RegEx Cleaner");
        llmCleanerRadio = new JRadioButton("LLM Cleaner");
        llmCleanerRadio.setSelected(true);
        
        ButtonGroup cleanerGroup = new ButtonGroup();
        cleanerGroup.add(regexCleanerRadio);
        cleanerGroup.add(llmCleanerRadio);
        
        cleanerPanel.add(regexCleanerRadio);
        cleanerPanel.add(llmCleanerRadio);
        
        // Database Operation Mode Panel
        JPanel dbModePanel = new JPanel(new GridLayout(0, 1, 2, 2));
        dbModePanel.setBorder(BorderFactory.createTitledBorder("Database Operation Mode"));
        
        appendModeRadio = new JRadioButton("Append to Existing Database");
        replaceModeRadio = new JRadioButton("Replace Existing Database");
        appendModeRadio.setSelected(true);
        
        ButtonGroup dbModeGroup = new ButtonGroup();
        dbModeGroup.add(appendModeRadio);
        dbModeGroup.add(replaceModeRadio);
        
        dbModePanel.add(appendModeRadio);
        dbModePanel.add(replaceModeRadio);
        
        // Export Panel
        JPanel exportPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        exportPanel.setBorder(BorderFactory.createTitledBorder("Database Actions"));
        
        exportButton = new JButton("Export Database");
        exportButton.addActionListener(e -> exportDatabase());
        exportPanel.add(exportButton);
        
        rightPanel.add(cleanerPanel);
        rightPanel.add(dbModePanel);
        rightPanel.add(exportPanel);

        // Split Pane for left and right panels
        JSplitPane splitPane = new JSplitPane(
            JSplitPane.HORIZONTAL_SPLIT,
            leftPanel,
            rightPanel
        );
        splitPane.setResizeWeight(0.6);

        // Control Panel
        JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
        startButton = new JButton("Start Refresh");
        startButton.setFont(new Font("Arial", Font.BOLD, 14));
        startButton.addActionListener(e -> startRefreshProcess());
        
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        
        controlPanel.add(splitPane, BorderLayout.CENTER);
        controlPanel.add(progressBar, BorderLayout.NORTH);
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

        mainPanel.add(controlPanel, BorderLayout.CENTER);
        mainPanel.add(logPanel, BorderLayout.SOUTH);

        frame.add(mainPanel, BorderLayout.CENTER);
    }

    private void startRefreshProcess() {
        startButton.setEnabled(false);
        progressBar.setValue(0);
        logArea.setText("");
        
        // Get cleaning pipeline selection
        final String cleaningPipeline = regexCleanerRadio.isSelected() ? "regex" : "llm";
        
        // Get database operation mode
        final boolean appendMode = appendModeRadio.isSelected();
        
        // Update page counts from spinners
        for (ScrapingSite site : scrapingSites) {
            JSpinner spinner = pageCountSpinners.get(site.getName());
            site.setPageCount((Integer) spinner.getValue());
        }
        
        // Create a background thread for the refresh process
        new Thread(() -> {
            try {
                scrapingService.startScraping(scrapingSites, cleaningPipeline, appendMode);
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

    private void exportDatabase() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Database");
        fileChooser.setSelectedFile(new File("database_export.json"));
        
        if (fileChooser.showSaveDialog(frame) == JFileChooser.APPROVE_OPTION) {
            try {
                // Determine which database file to export based on last used cleaning pipeline
                String sourceFileName = regexCleanerRadio.isSelected() ? 
                    "database/database1.json" : "database/database.json";
                    
                File sourceFile = new File(sourceFileName);
                Path sourcePath = sourceFile.toPath();
                Path targetPath = fileChooser.getSelectedFile().toPath();
                
                Files.copy(sourcePath, targetPath, StandardCopyOption.REPLACE_EXISTING);
                
                JOptionPane.showMessageDialog(frame,
                    "Database exported successfully!",
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(frame,
                    "Error exporting database: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            }
        }
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
