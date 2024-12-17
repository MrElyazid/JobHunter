package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import com.jobhunter.pages.statistics.services.DatabaseQueryService;
import com.jobhunter.pages.statistics.services.ChartService;

public class OverviewPanel extends JPanel {
    
    public OverviewPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Summary statistics panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        addStatCard(summaryPanel, "Total Job Listings", DatabaseQueryService.getTotalJobCount());
        addStatCard(summaryPanel, "Average Salary", DatabaseQueryService.getAverageSalary());
        addStatCard(summaryPanel, "Most Common Contract", DatabaseQueryService.getMostCommonContract());
        addStatCard(summaryPanel, "Remote Jobs %", DatabaseQueryService.getRemoteJobsPercentage());

        // Add charts
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.add(ChartService.createContractTypeChart());
        chartsPanel.add(ChartService.createCompanyTypeChart());

        add(summaryPanel, BorderLayout.NORTH);
        add(chartsPanel, BorderLayout.CENTER);
    }

    private void addStatCard(JPanel panel, String title, String value) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Arial", Font.PLAIN, 14));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Arial", Font.BOLD, 18));
        valueLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createVerticalStrut(5));
        card.add(valueLabel);
        panel.add(card);
    }
}
