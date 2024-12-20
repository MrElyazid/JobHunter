package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import com.jobhunter.pages.statistics.services.ChartService;

public class SkillsAnalysisPanel extends JPanel {
    
    public SkillsAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Create panel for charts
        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        
        // Add top skills chart
        chartsPanel.add(ChartService.createTopSkillsChart());
        
        // Add skills by sector chart
        //chartsPanel.add(ChartService.createSkillsBySectorChart());

        add(chartsPanel, BorderLayout.CENTER);
    }
}
