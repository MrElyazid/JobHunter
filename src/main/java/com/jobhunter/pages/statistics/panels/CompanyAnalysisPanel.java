package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class CompanyAnalysisPanel extends JPanel {
    
    public CompanyAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Description label
        JLabel descLabel = new JLabel("Analysis of Company Types");
        descLabel.setFont(new Font("Arial", Font.BOLD, 16));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(descLabel, BorderLayout.NORTH);

        // Company types chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(ChartService.createCompanyTypeChart(), BorderLayout.CENTER);
        
        // Add description
        JTextArea description = new JTextArea(
            "This chart shows the distribution between local and foreign companies posting job opportunities. " +
            "Understanding this distribution can help job seekers who have specific preferences for working with " +
            "either local or international organizations. Only companies with verified status are included in this analysis."
        );
        description.setWrapStyleWord(true);
        description.setLineWrap(true);
        description.setOpaque(false);
        description.setEditable(false);
        description.setFont(new Font("Arial", Font.PLAIN, 12));
        description.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add trends chart
        JPanel trendsPanel = new JPanel(new BorderLayout());
        trendsPanel.add(ChartService.createJobTrendsChart(), BorderLayout.CENTER);
        
        // Create split panel
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPane.setTopComponent(chartPanel);
        splitPane.setBottomComponent(trendsPanel);
        splitPane.setResizeWeight(0.5);
        
        add(splitPane, BorderLayout.CENTER);
        add(description, BorderLayout.SOUTH);
    }
}
