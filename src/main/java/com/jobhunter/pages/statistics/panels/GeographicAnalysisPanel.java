package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class GeographicAnalysisPanel extends JPanel {
    
    public GeographicAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Description label
        JLabel descLabel = new JLabel("Geographic Distribution of Job Opportunities");
        descLabel.setFont(new Font("Arial", Font.BOLD, 16));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(descLabel, BorderLayout.NORTH);

        // Jobs by city chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(ChartService.createJobsByCityChart(), BorderLayout.CENTER);
        
        // Add description
        JTextArea description = new JTextArea(
            "This chart displays the distribution of job opportunities across different cities in Morocco. " +
            "The data shows the top cities by number of job listings, helping to identify the major employment hubs. " +
            "Only cities with a significant number of job postings are shown to ensure data reliability."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setOpaque(false);
        description.setEditable(false);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        chartPanel.add(description, BorderLayout.SOUTH);
        add(chartPanel, BorderLayout.CENTER);
    }
}
