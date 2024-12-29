package com.jobhunter.pages.main;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.refreshDb.RefreshDbPage;
import com.jobhunter.pages.browse.BrowseJobsPage;
import com.jobhunter.pages.statistics.StatisticsPage;
import com.jobhunter.pages.regression.RegressionModelsPage;

public class MainPage {
    private JFrame frame;
    private static MainPage instance;

    public static MainPage getInstance() {
        if (instance == null) {
            instance = new MainPage();
        }
        return instance;
    }

    private MainPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("JobHunter");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));
        frame.getContentPane().setBackground(new Color(240, 245, 250)); // Light blue-gray background

        // Banner Panel
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        bannerPanel.setBackground(new Color(25, 118, 210)); // Material blue
        
        JLabel titleLabel = new JLabel("JobHunter", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 40));
        titleLabel.setForeground(Color.WHITE);
        
        JLabel subtitleLabel = new JLabel("Your Moroccan Job Market Analysis Tool", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Segoe UI Light", Font.PLAIN, 20));
        subtitleLabel.setForeground(new Color(224, 236, 255)); // Light blue text
        
        bannerPanel.add(titleLabel, BorderLayout.CENTER);
        bannerPanel.add(subtitleLabel, BorderLayout.SOUTH);
        frame.add(bannerPanel, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        // Create buttons with icons and descriptions
        addButton(buttonPanel, gbc, 0,
            "Refresh Database",
            "Update job listings from all sources",
            e -> openRefreshDb());
            
        addButton(buttonPanel, gbc, 1,
            "Browse Jobs",
            "Search and filter job listings",
            e -> openBrowseJobs());
  
        addButton(buttonPanel, gbc, 3,
            "Statistics",
            "View market trends and analysis",
            e -> openStatistics());

        addButton(buttonPanel, gbc, 4,
            "ML Models",
            "Predict salaries and find matching jobs",
            e -> openRegressionModels());

        // Add padding panel
        gbc.weighty = 1.0;
        gbc.gridy = 5;
        buttonPanel.add(new JPanel(), gbc);

        // Wrap button panel in another panel with padding
        JPanel centeredPanel = new JPanel(new BorderLayout());
        centeredPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        centeredPanel.setBackground(new Color(240, 245, 250)); // Match frame background
        centeredPanel.add(buttonPanel, BorderLayout.CENTER);
        
        frame.add(centeredPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        footerPanel.setBackground(new Color(25, 118, 210)); // Material blue
        JLabel footerLabel = new JLabel("© 2024 JobHunter - All Rights Reserved");
        footerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        footerLabel.setForeground(Color.WHITE);
        footerPanel.add(footerLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);
    }

    private void addButton(JPanel panel, GridBagConstraints gbc, int index,
                         String title, String description, java.awt.event.ActionListener listener) {
        gbc.gridy = index;
        
        JPanel buttonContainer = new JPanel(new BorderLayout(10, 5));
        buttonContainer.setBackground(Color.WHITE);
        buttonContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JButton button = new JButton(title);
        button.setFont(new Font("Segoe UI", Font.BOLD, 16));
        button.setForeground(new Color(25, 118, 210)); // Material blue
        button.setBackground(Color.WHITE);
        button.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.addActionListener(listener);
        
        // Add hover effect
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setBackground(new Color(232, 240, 254)); // Light blue background on hover
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setBackground(Color.WHITE);
            }
        });
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        descLabel.setForeground(new Color(97, 97, 97)); // Material gray
        
        buttonContainer.add(button, BorderLayout.CENTER);
        buttonContainer.add(descLabel, BorderLayout.SOUTH);
        
        panel.add(buttonContainer, gbc);
    }

    private void openRefreshDb() {
        frame.setVisible(false);
        RefreshDbPage refreshPage = new RefreshDbPage();
        refreshPage.show();
    }

    private void openBrowseJobs() {
        frame.setVisible(false);
        BrowseJobsPage browsePage = new BrowseJobsPage();
        browsePage.show();
    }


    private void openStatistics() {
        frame.setVisible(false);
        StatisticsPage statsPage = new StatisticsPage();
        statsPage.show();
    }

    private void openRegressionModels() {
        frame.setVisible(false);
        RegressionModelsPage modelsPage = new RegressionModelsPage();
        modelsPage.show();
    }

    public void show() {
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                MainPage window = MainPage.getInstance();
                window.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
