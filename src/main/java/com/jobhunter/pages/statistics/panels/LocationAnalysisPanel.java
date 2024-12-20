package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class LocationAnalysisPanel extends JPanel {
    
    public LocationAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create panel for charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Add jobs by city chart
        chartsPanel.add(ChartService.createJobsByCityChart());
        
        // Add salary by location chart
        //chartsPanel.add(ChartService.createSalaryByLocationChart());

        add(chartsPanel, BorderLayout.CENTER);
    }
}
