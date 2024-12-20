package com.jobhunter.pages.statistics.panels;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.jobhunter.util.DatabaseConnection;

public class LanguageAnalysisPanel extends JPanel {
    
    public LanguageAnalysisPanel() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initialize();
    }

    private void initialize() {
        // 1. Most Required Languages
        add(createLanguageDistributionChart());
        
        // 2. Language Proficiency Levels
        add(createProficiencyDistributionChart());
        
        // 3. Languages by Sector
        add(createSectorLanguageChart());
        
        // 4. Language Requirements vs Salary
        add(createLanguageSalaryChart());
    }

    private JPanel createLanguageDistributionChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT languages, COUNT(*) as count FROM job_post " +
                "WHERE languages IS NOT NULL AND languages != '' " +
                "GROUP BY languages ORDER BY count DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dataset.setValue(rs.getString("languages"), rs.getInt("count"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Most Required Languages",
            dataset,
            true,  // legend
            true,  // tooltips
            false  // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createProficiencyDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT languages, language_profeciency, COUNT(*) as count " +
                "FROM job_post " +
                "WHERE languages IS NOT NULL AND languages != '' " +
                "AND language_profeciency IS NOT NULL " +
                "GROUP BY languages, language_profeciency " +
                "ORDER BY count DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"),
                               rs.getString("language_profeciency"),
                               rs.getString("languages"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
            "Language Proficiency Requirements",
            "Language",
            "Number of Jobs",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createSectorLanguageChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT sector, languages, COUNT(*) as count " +
                "FROM job_post " +
                "WHERE sector IS NOT NULL AND languages IS NOT NULL " +
                "AND languages != '' " +
                "GROUP BY sector, languages " +
                "HAVING count > 5 " +  // Filter out rare combinations
                "ORDER BY sector, count DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"),
                               rs.getString("languages"),
                               rs.getString("sector"));
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
            "Language Requirements by Sector",
            "Sector",
            "Number of Jobs",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createLanguageSalaryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT languages, language_profeciency, " +
                "AVG(min_salary) as avg_salary " +
                "FROM job_post " +
                "WHERE languages IS NOT NULL AND languages != '' " +
                "AND language_profeciency IS NOT NULL " +
                "AND min_salary > 0 " +
                "GROUP BY languages, language_profeciency " +
                "HAVING COUNT(*) > 3 " +  // Filter out rare combinations
                "ORDER BY avg_salary DESC")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String language = rs.getString("languages");
                String proficiency = rs.getString("language_profeciency");
                dataset.addValue(rs.getDouble("avg_salary"),
                               proficiency,
                               language);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Average Salary by Language and Proficiency",
            "Language",
            "Average Salary (DH)",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }
}
