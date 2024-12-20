package com.jobhunter.pages.regression;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.main.MainPage;
import com.jobhunter.pages.regression.panels.*;

public class RegressionModelsPage {
    private JFrame frame;
    private JTabbedPane tabbedPane;

    public RegressionModelsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("ML Models & Predictions");
        frame.setBounds(100, 100, 1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Top Panel with Back Button and Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            MainPage.getInstance().show();
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("Machine Learning Models & Predictions", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(300));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane for different models
        tabbedPane = new JTabbedPane();
        
        // Salary Prediction - Predict salary based on sector, city, experience
        tabbedPane.addTab("Salary Prediction", new SalaryPredictionPanel());
        
        // Sector Prediction - Predict suitable sectors based on desired salary and experience
        tabbedPane.addTab("Sector Prediction", new SectorPredictionPanel());
        
        // Job Recommendation - Form to find best matching jobs
        tabbedPane.addTab("Job Recommendations", new JobRecommendationPanel());

        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }
}
