package com.jobhunter.pages.main;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.refreshDb.RefreshDbPage;
import com.jobhunter.pages.browse.BrowseJobsPage;
import com.jobhunter.pages.chatbot.ChatbotPage;
import com.jobhunter.pages.statistics.StatisticsPage;

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

        // Banner Panel
        JPanel bannerPanel = new JPanel(new BorderLayout());
        bannerPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        JLabel titleLabel = new JLabel("JobHunter", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        
        JLabel subtitleLabel = new JLabel("Your Moroccan Job Market Analysis Tool", SwingConstants.CENTER);
        subtitleLabel.setFont(new Font("Arial", Font.PLAIN, 18));
        
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
            
        addButton(buttonPanel, gbc, 2,
            "AI Chatbot",
            "Get intelligent job market insights",
            e -> openChatbot());
            
        addButton(buttonPanel, gbc, 3,
            "Statistics & ML",
            "View market trends and analysis",
            e -> openStatistics());

        // Add padding panel
        gbc.weighty = 1.0;
        gbc.gridy = 4;
        buttonPanel.add(new JPanel(), gbc);

        // Wrap button panel in another panel with padding
        JPanel centeredPanel = new JPanel(new BorderLayout());
        centeredPanel.setBorder(BorderFactory.createEmptyBorder(20, 60, 20, 60));
        centeredPanel.add(buttonPanel, BorderLayout.CENTER);
        
        frame.add(centeredPanel, BorderLayout.CENTER);

        // Footer
        JPanel footerPanel = new JPanel();
        JLabel footerLabel = new JLabel("© 2023 JobHunter - All Rights Reserved");
        footerLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        footerPanel.add(footerLabel);
        frame.add(footerPanel, BorderLayout.SOUTH);
    }

    private void addButton(JPanel panel, GridBagConstraints gbc, int index,
                         String title, String description, java.awt.event.ActionListener listener) {
        gbc.gridy = index;
        
        JPanel buttonContainer = new JPanel(new BorderLayout(10, 5));
        buttonContainer.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        
        JButton button = new JButton(title);
        button.setFont(new Font("Arial", Font.BOLD, 16));
        button.addActionListener(listener);
        
        JLabel descLabel = new JLabel(description);
        descLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        descLabel.setForeground(Color.GRAY);
        
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

    private void openChatbot() {
        frame.setVisible(false);
        ChatbotPage chatbotPage = new ChatbotPage();
        chatbotPage.show();
    }

    private void openStatistics() {
        frame.setVisible(false);
        StatisticsPage statsPage = new StatisticsPage();
        statsPage.show();
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
