package com.jobhunter.pages.statistics;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.panels.*;
import com.jobhunter.pages.main.MainPage;

public class StatisticsPage {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JButton backButton;

    public StatisticsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Statistics and ML");
        frame.setBounds(100, 100, 1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Top Panel with Back Button and Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            MainPage.getInstance().show();
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("Job Market Statistics & Analysis", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(300));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane for different statistics
        tabbedPane = new JTabbedPane();
        
        // Add different statistics tabs
        tabbedPane.addTab("Overview", new OverviewPanel());
        tabbedPane.addTab("Salary Analysis", new SalaryAnalysisPanel());
        tabbedPane.addTab("Skills Analysis", new SkillsAnalysisPanel());
        tabbedPane.addTab("Location Analysis", new LocationAnalysisPanel());
        tabbedPane.addTab("Trends", new TrendsPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }
}
