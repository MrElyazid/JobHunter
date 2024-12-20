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
import org.json.JSONArray;
import com.jobhunter.util.DatabaseConnection;

public class EducationPanel extends JPanel {
    
    public EducationPanel() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        initialize();
    }

    private void initialize() {
        // 1. Most Required Diplomas
        add(createDiplomaDistributionChart());
        
        // 2. Education Level by Sector
        add(createSectorEducationChart());
        
        // 3. Education-Salary Correlation
        add(createEducationSalaryChart());
        
        // 4. Education-Experience Relationship
        add(createEducationExperienceChart());
    }

    private JPanel createDiplomaDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT diploma, COUNT(*) as count FROM job_post " +
                "WHERE diploma IS NOT NULL AND diploma != '[]' " +
                "GROUP BY diploma ORDER BY count DESC LIMIT 10")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String diplomaJson = rs.getString("diploma");
                JSONArray diplomas = new JSONArray(diplomaJson);
                for (int i = 0; i < diplomas.length(); i++) {
                    String diploma = diplomas.getString(i);
                    dataset.addValue(rs.getInt("count"), "Required", diploma);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Most Required Educational Qualifications",
            "Diploma Type",
            "Number of Jobs",
            dataset,
            PlotOrientation.VERTICAL,
            false,  // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createSectorEducationChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT sector, diploma FROM job_post " +
                "WHERE sector IS NOT NULL AND diploma IS NOT NULL AND diploma != '[]' " +
                "ORDER BY sector LIMIT 1000")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String sector = rs.getString("sector");
                String diplomaJson = rs.getString("diploma");
                JSONArray diplomas = new JSONArray(diplomaJson);
                for (int i = 0; i < diplomas.length(); i++) {
                    String diploma = diplomas.getString(i);
                    dataset.incrementValue(1.0, diploma, sector);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createStackedBarChart(
            "Education Requirements by Sector",
            "Sector",
            "Number of Requirements",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createEducationSalaryChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT diploma, " +
                "AVG(min_salary) as avg_salary, " +
                "MIN(min_salary) as min_salary, " +
                "MAX(min_salary) as max_salary " +
                "FROM job_post " +
                "WHERE diploma IS NOT NULL AND diploma != '[]' " +
                "AND min_salary > 0 " +
                "GROUP BY diploma ORDER BY avg_salary DESC LIMIT 10")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String diplomaJson = rs.getString("diploma");
                JSONArray diplomas = new JSONArray(diplomaJson);
                for (int i = 0; i < diplomas.length(); i++) {
                    String diploma = diplomas.getString(i);
                    dataset.addValue(rs.getDouble("min_salary"), "Minimum", diploma);
                    dataset.addValue(rs.getDouble("avg_salary"), "Average", diploma);
                    dataset.addValue(rs.getDouble("max_salary"), "Maximum", diploma);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Salary Ranges by Education Level",
            "Education Level",
            "Salary (DH)",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }

    private JPanel createEducationExperienceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT diploma, AVG(min_experience) as avg_experience " +
                "FROM job_post " +
                "WHERE diploma IS NOT NULL AND diploma != '[]' " +
                "AND min_experience > 0 " +
                "GROUP BY diploma ORDER BY avg_experience DESC LIMIT 10")) {
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String diplomaJson = rs.getString("diploma");
                JSONArray diplomas = new JSONArray(diplomaJson);
                for (int i = 0; i < diplomas.length(); i++) {
                    String diploma = diplomas.getString(i);
                    dataset.addValue(rs.getDouble("avg_experience"), 
                                   "Average Required Experience (Years)", 
                                   diploma);
                }
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Average Required Experience by Education Level",
            "Education Level",
            "Years of Experience",
            dataset,
            PlotOrientation.VERTICAL,
            true,   // legend
            true,   // tooltips
            false   // urls
        );

        return new ChartPanel(chart);
    }
}
