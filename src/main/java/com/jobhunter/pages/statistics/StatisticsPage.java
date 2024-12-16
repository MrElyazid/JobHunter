package com.jobhunter.pages.statistics;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.jobhunter.util.DatabaseConnection;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

public class StatisticsPage {
    private JFrame frame;
    private JTabbedPane tabbedPane;
    private JButton backButton;

    public StatisticsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Statistics and ML");
        frame.setBounds(100, 100, 1000, 700);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Top Panel with Back Button and Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            // TODO: Navigate back to main page
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("Job Market Statistics & Analysis", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(300));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Create tabbed pane for different statistics
        tabbedPane = new JTabbedPane();
        
        // Add different statistics tabs
        addOverviewTab();
        addSalaryAnalysisTab();
        addSkillsAnalysisTab();
        addLocationAnalysisTab();
        addTrendsTab();

        frame.add(tabbedPane, BorderLayout.CENTER);
    }

    private void addOverviewTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Summary statistics panel
        JPanel summaryPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        addStatCard(summaryPanel, "Total Job Listings", getTotalJobCount());
        addStatCard(summaryPanel, "Average Salary", getAverageSalary());
        addStatCard(summaryPanel, "Most Common Contract", getMostCommonContract());
        addStatCard(summaryPanel, "Remote Jobs %", getRemoteJobsPercentage());

        // Add charts
        JPanel chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.add(createContractTypeChart());
        chartsPanel.add(createCompanyTypeChart());

        panel.add(summaryPanel, BorderLayout.NORTH);
        panel.add(chartsPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Overview", panel);
    }

    private void addSalaryAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Salary distribution chart
        ChartPanel salaryDistChart = createSalaryDistributionChart();
        
        // Salary by experience level
        ChartPanel salaryByExpChart = createSalaryByExperienceChart();

        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartsPanel.add(salaryDistChart);
        chartsPanel.add(salaryByExpChart);

        panel.add(chartsPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Salary Analysis", panel);
    }

    private void addSkillsAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Top skills chart
        ChartPanel topSkillsChart = createTopSkillsChart();
        
        // Skills by sector
        ChartPanel skillsBySectorChart = createSkillsBySectorChart();

        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartsPanel.add(topSkillsChart);
        chartsPanel.add(skillsBySectorChart);

        panel.add(chartsPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Skills Analysis", panel);
    }

    private void addLocationAnalysisTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Jobs by city chart
        ChartPanel jobsByCityChart = createJobsByCityChart();
        
        // Salary by location
        ChartPanel salaryByLocationChart = createSalaryByLocationChart();

        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartsPanel.add(jobsByCityChart);
        chartsPanel.add(salaryByLocationChart);

        panel.add(chartsPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Location Analysis", panel);
    }

    private void addTrendsTab() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Trending skills chart
        ChartPanel trendingSkillsChart = createTrendingSkillsChart();
        
        // Job postings over time
        ChartPanel jobTrendsChart = createJobTrendsChart();

        JPanel chartsPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        chartsPanel.add(trendingSkillsChart);
        chartsPanel.add(jobTrendsChart);

        panel.add(chartsPanel, BorderLayout.CENTER);

        tabbedPane.addTab("Trends", panel);
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

    // Database query methods
    private String getTotalJobCount() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM job_post")) {
            if (rs.next()) {
                return String.format("%,d", rs.getInt(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getAverageSalary() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT AVG(min_salary) FROM job_post WHERE min_salary > 0")) {
            if (rs.next()) {
                return String.format("%,.0f MAD", rs.getDouble(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getMostCommonContract() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT contract_type, COUNT(*) as count " +
                "FROM job_post " +
                "GROUP BY contract_type " +
                "ORDER BY count DESC LIMIT 1")) {
            if (rs.next()) {
                return rs.getString("contract_type");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getRemoteJobsPercentage() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT (COUNT(CASE WHEN is_remote = true THEN 1 END) * 100.0 / COUNT(*)) " +
                "FROM job_post")) {
            if (rs.next()) {
                return String.format("%.1f%%", rs.getDouble(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    // Chart creation methods
    private ChartPanel createContractTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT contract_type, COUNT(*) as count " +
                "FROM job_post " +
                "GROUP BY contract_type " +
                "ORDER BY count DESC")) {
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

    private ChartPanel createCompanyTypeChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT foriegn_company, COUNT(*) as count " +
                "FROM job_post " +
                "GROUP BY foriegn_company")) {
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

    private ChartPanel createSalaryDistributionChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT " +
                "CASE " +
                "  WHEN min_salary < 5000 THEN '0-5k' " +
                "  WHEN min_salary < 10000 THEN '5k-10k' " +
                "  WHEN min_salary < 15000 THEN '10k-15k' " +
                "  WHEN min_salary < 20000 THEN '15k-20k' " +
                "  ELSE '20k+' " +
                "END as salary_range, " +
                "COUNT(*) as count " +
                "FROM job_post " +
                "WHERE min_salary > 0 " +
                "GROUP BY salary_range " +
                "ORDER BY salary_range")) {
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

    private ChartPanel createSalaryByExperienceChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT min_experience, AVG(min_salary) as avg_salary " +
                "FROM job_post " +
                "WHERE min_salary > 0 " +
                "GROUP BY min_experience " +
                "ORDER BY min_experience")) {
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

    private ChartPanel createTopSkillsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: Implement query for top skills from hard_skills JSON array
        return new ChartPanel(ChartFactory.createBarChart(
            "Top Required Skills",
            "Skill",
            "Number of Jobs",
            dataset
        ));
    }

    private ChartPanel createSkillsBySectorChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: Implement query for skills distribution by sector
        return new ChartPanel(ChartFactory.createBarChart(
            "Skills Distribution by Sector",
            "Sector",
            "Number of Jobs",
            dataset
        ));
    }

    private ChartPanel createJobsByCityChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT location, COUNT(*) as count " +
                "FROM job_post " +
                "GROUP BY location " +
                "ORDER BY count DESC " +
                "LIMIT 10")) {
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

    private ChartPanel createSalaryByLocationChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                "SELECT location, AVG(min_salary) as avg_salary " +
                "FROM job_post " +
                "WHERE min_salary > 0 " +
                "GROUP BY location " +
                "HAVING COUNT(*) > 5 " +
                "ORDER BY avg_salary DESC " +
                "LIMIT 10")) {
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

    private ChartPanel createTrendingSkillsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: Implement query for trending skills over time
        return new ChartPanel(ChartFactory.createLineChart(
            "Trending Skills Over Time",
            "Time Period",
            "Demand (Job Count)",
            dataset
        ));
    }

    private ChartPanel createJobTrendsChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        // TODO: Implement query for job posting trends over time
        return new ChartPanel(ChartFactory.createLineChart(
            "Job Postings Over Time",
            "Time Period",
            "Number of Jobs Posted",
            dataset
        ));
    }

    public void show() {
        frame.setVisible(true);
    }
}
