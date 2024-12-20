package com.jobhunter.pages.statistics.panels;

import javax.swing.*;
import java.awt.*;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.general.DefaultPieDataset;
import com.jobhunter.pages.statistics.services.DatabaseQueryService;
import com.jobhunter.util.DatabaseConnection;
import java.sql.*;

public class SourceAnalysisPanel extends JPanel {
    
    public SourceAnalysisPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Description label
        JLabel descLabel = new JLabel("Job Posting Sources Analysis");
        descLabel.setFont(new Font("Arial", Font.BOLD, 16));
        descLabel.setHorizontalAlignment(SwingConstants.CENTER);
        add(descLabel, BorderLayout.NORTH);

        // Create source distribution chart
        JPanel chartPanel = new JPanel(new BorderLayout());
        chartPanel.add(createSourceDistributionChart(), BorderLayout.CENTER);
        
        // Add description
        JTextArea description = new JTextArea(
            "This chart shows the distribution of job postings across different job portals. " +
            "Understanding which platforms have the most job listings can help focus your job search efforts. " +
            "The data includes postings from major Moroccan job portals like Rekrute, Anapec, EmploiMa, and others."
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

    private ChartPanel createSourceDistributionChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COALESCE(source, 'Unknown') as source, COUNT(*) as count " +
                 "FROM job_post " +
                 "WHERE source IS NOT NULL " +
                 "GROUP BY source " +
                 "HAVING count > 5 " +
                 "ORDER BY count DESC")) {
            
            boolean hasData = false;
            while (rs.next()) {
                String source = rs.getString("source");
                int count = rs.getInt("count");
                if (source != null && !source.trim().isEmpty()) {
                    dataset.setValue(source, count);
                    hasData = true;
                }
            }
            
            if (!hasData) {
                dataset.setValue("No Data Available", 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.setValue("Error Loading Data", 1);
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Distribution by Source",
            dataset,
            true,
            true,
            false
        );

        return new ChartPanel(chart);
    }
}
