package com.jobhunter;

import javax.swing.*;
import java.awt.*;

public class App_backup {
    private JFrame frame;

    public App_backup() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Job Scraper");
        frame.setBounds(100, 100, 300, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        // Banner
        JLabel banner = new JLabel("JobHunter", SwingConstants.CENTER);
        banner.setFont(new Font("Arial", Font.BOLD, 24));
        frame.add(banner, BorderLayout.NORTH);

        // Button Panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(4, 1, 10, 10));

        JButton refreshDbButton = new JButton("Refresh DB");
        JButton browseJobsButton = new JButton("Browse Jobs");
        JButton chatbotButton = new JButton("Chatbot");
        JButton statisticsButton = new JButton("Statistics and ML");

        buttonPanel.add(refreshDbButton);
        buttonPanel.add(browseJobsButton);
        buttonPanel.add(chatbotButton);
        buttonPanel.add(statisticsButton);

        frame.add(buttonPanel, BorderLayout.CENTER);

        // Action Listeners (placeholders for now)
        refreshDbButton.addActionListener(e -> {
            // Navigate to Refresh DB page
        });

        browseJobsButton.addActionListener(e -> {
            // Navigate to Browse Jobs page
        });

        chatbotButton.addActionListener(e -> {
            // Navigate to Chatbot page
        });

        statisticsButton.addActionListener(e -> {
            // Navigate to Statistics and ML page
        });
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                App_backup window = new App_backup();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
