package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class SalaryAnalysisPanel extends JPanel {
    
    public SalaryAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Salary distribution chart
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Add salary distribution chart
        //chartsPanel.add(ChartService.createSalaryDistributionChart());
        
        // Add salary by experience chart
        //chartsPanel.add(ChartService.createSalaryByExperienceChart());

        add(chartsPanel, BorderLayout.CENTER);
    }
}
