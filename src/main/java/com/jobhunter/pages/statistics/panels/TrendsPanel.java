package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class TrendsPanel extends JPanel {
    
    public TrendsPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create panel for charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Add trending skills chart
        //chartsPanel.add(ChartService.createTrendingSkillsChart());
        
        // Add job trends chart
        chartsPanel.add(ChartService.createJobTrendsChart());

        add(chartsPanel, BorderLayout.CENTER);
    }
}
