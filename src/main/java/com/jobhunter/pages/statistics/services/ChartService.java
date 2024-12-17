package com.jobhunter.pages.statistics.services;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChartService {
    
    public static ChartPanel createContractTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            ResultSet rs = DatabaseQueryService.getContractTypeDistribution();
            while (rs.next()) {
                dataset.setValue(rs.getString("contract_type"), rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Contract Types Distribution",
            dataset,
            true,
            true,
            false
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createCompanyTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try {
            ResultSet rs = DatabaseQueryService.getCompanyTypeDistribution();
            while (rs.next()) {
                String type = rs.getBoolean("foriegn_company") ? "Foreign" : "Local";
                dataset.setValue(type, rs.getInt("count"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createPieChart(
            "Company Types",
            dataset,
            true,
            true,
            false
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createSalaryDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getSalaryDistribution();
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Salary Distribution", 
                               rs.getString("salary_range"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Salary Distribution",
            "Salary Range (MAD)",
            "Number of Jobs",
            dataset
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createSalaryByExperienceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getSalaryByExperience();
            while (rs.next()) {
                dataset.addValue(rs.getDouble("avg_salary"), "Average Salary", 
                               rs.getInt("min_experience") + " years");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createLineChart(
            "Average Salary by Experience",
            "Years of Experience",
            "Average Salary (MAD)",
            dataset
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createJobsByCityChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getJobsByCity();
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Jobs", rs.getString("location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top 10 Cities by Job Count",
            "City",
            "Number of Jobs",
            dataset
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createSalaryByLocationChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getSalaryByLocation();
            while (rs.next()) {
                dataset.addValue(rs.getDouble("avg_salary"), "Average Salary", 
                               rs.getString("location"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Average Salary by Location",
            "City",
            "Average Salary (MAD)",
            dataset
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createTopSkillsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getTopSkills();
            while (rs.next()) {
                dataset.addValue(rs.getInt("count"), "Demand", rs.getString("skill"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ChartPanel(ChartFactory.createBarChart(
            "Top Required Skills",
            "Skill",
            "Number of Jobs",
            dataset
        ));
    }

    public static ChartPanel createSkillsBySectorChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getSkillsBySector();
            while (rs.next()) {
                dataset.addValue(
                    rs.getDouble("percentage"),
                    rs.getString("sector"),
                    rs.getString("skill")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ChartPanel(ChartFactory.createBarChart(
            "Skills Distribution by Sector",
            "Skill",
            "Percentage of Jobs (%)",
            dataset
        ));
    }

    public static ChartPanel createTrendingSkillsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getTrendingSkills();
            while (rs.next()) {
                dataset.addValue(
                    rs.getInt("demand_count"),
                    "Demand",
                    rs.getString("skill")
                );
                dataset.addValue(
                    rs.getDouble("avg_salary") / 1000, // Convert to thousands for better visualization
                    "Avg Salary (K MAD)",
                    rs.getString("skill")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ChartPanel(ChartFactory.createLineChart(
            "Trending Skills (Demand vs Salary)",
            "Skills",
            "Demand (Jobs) / Salary (K MAD)",
            dataset
        ));
    }

    public static ChartPanel createJobTrendsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getJobTrends();
            while (rs.next()) {
                String sector = rs.getString("sector");
                dataset.addValue(rs.getInt("job_count"), "Job Count", sector);
                dataset.addValue(rs.getDouble("remote_percentage"), "Remote %", sector);
                dataset.addValue(rs.getDouble("foreign_percentage"), "Foreign %", sector);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return new ChartPanel(ChartFactory.createLineChart(
            "Job Market Trends by Sector",
            "Sector",
            "Count / Percentage",
            dataset
        ));
    }
}
