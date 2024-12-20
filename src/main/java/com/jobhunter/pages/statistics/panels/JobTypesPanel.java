package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class JobTypesPanel extends JPanel {
    
    public JobTypesPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Description label
        JLabel descLabel = new JLabel("Distribution of Job Types and Contract Types");
        descLabel.setFont(new Font("Arial", Font.BOLD, 16));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(descLabel, BorderLayout.NORTH);

        // Contract types chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(ChartService.createContractTypeChart(), BorderLayout.CENTER);
        
        // Add description
        JTextArea description = new JTextArea(
            "This chart shows the distribution of different contract types across all job listings. " +
            "Contract types include CDI (permanent), CDD (fixed-term), internships, and other variations. " +
            "Understanding the prevalence of different contract types can help job seekers target their search effectively."
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
