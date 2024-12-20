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
                String contractType = rs.getString("contract_type");
                if (contractType != null && !contractType.trim().isEmpty()) {
                    dataset.setValue(contractType, rs.getInt("count"));
                }
            }
            // Add a check for empty dataset
            if (dataset.getItemCount() == 0) {
                dataset.setValue("No Data Available", 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.setValue("Error Loading Data", 1);
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
            int localCount = 0;
            int foreignCount = 0;
            int totalProcessed = 0;
            
            while (rs.next()) {
                boolean isForeign = rs.getBoolean("foriegn_company");
                int count = rs.getInt("count");
                if (!rs.wasNull()) {
                    if (isForeign) {
                        foreignCount += count;
                    } else {
                        localCount += count;
                    }
                    totalProcessed += count;
                }
            }
            
            if (totalProcessed > 0) {
                if (localCount > 0) dataset.setValue("Local", localCount);
                if (foreignCount > 0) dataset.setValue("Foreign", foreignCount);
            } else {
                dataset.setValue("No Data Available", 1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.setValue("Error Loading Data", 1);
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

    public static ChartPanel createJobsByCityChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getJobsByCity();
            boolean hasData = false;
            while (rs.next()) {
                String location = rs.getString("location");
                int count = rs.getInt("count");
                if (location != null && !location.equals("Unknown") && !location.trim().isEmpty()) {
                    dataset.addValue(count, "Jobs", location);
                    hasData = true;
                }
            }
            if (!hasData) {
                dataset.addValue(0, "No Location Data Available", "N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.addValue(0, "Error Loading Data", "Error");
        }

        JFreeChart chart = ChartFactory.createBarChart(
            "Top Cities by Job Count",
            "City",
            "Number of Jobs",
            dataset
        );

        return new ChartPanel(chart);
    }

    public static ChartPanel createTopSkillsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getTopSkills();
            boolean hasData = false;
            while (rs.next()) {
                String skill = rs.getString("skill");
                int count = rs.getInt("count");
                if (skill != null && !skill.trim().isEmpty() && count > 0) {
                    dataset.addValue(count, "Demand", skill);
                    hasData = true;
                }
            }
            if (!hasData) {
                dataset.addValue(0, "No Skills Data Available", "N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.addValue(0, "Error Loading Data", "Error");
        }

        return new ChartPanel(ChartFactory.createBarChart(
            "Top Required Skills",
            "Skill",
            "Number of Jobs",
            dataset
        ));
    }

    public static ChartPanel createJobTrendsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try {
            ResultSet rs = DatabaseQueryService.getJobTrends();
            boolean hasData = false;
            while (rs.next()) {
                String sector = rs.getString("sector");
                if (sector != null && !sector.trim().isEmpty()) {
                    int jobCount = rs.getInt("job_count");
                    double remotePercentage = rs.getDouble("remote_percentage");
                    if (jobCount > 0) {
                        dataset.addValue(jobCount, "Job Count", sector);
                        if (!rs.wasNull()) {
                            dataset.addValue(remotePercentage, "Remote %", sector);
                        }
                        hasData = true;
                    }
                }
            }
            if (!hasData) {
                dataset.addValue(0, "No Trend Data Available", "N/A");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            dataset.addValue(0, "Error Loading Data", "Error");
        }

        return new ChartPanel(ChartFactory.createBarChart(
            "Job Market Trends by Sector",
            "Sector",
            "Count / Percentage",
            dataset
        ));
    }
}
