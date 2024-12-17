package com.jobhunter.pages.browse;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.Vector;
import com.jobhunter.util.DatabaseConnection;
import com.jobhunter.pages.main.MainPage;
import com.jobhunter.pages.browse.panels.*;
import com.jobhunter.pages.browse.services.JobService;

public class BrowseJobsPage {
    private JFrame frame;
    private FiltersPanel filtersPanel;
    private JobsTablePanel jobsTablePanel;
    private JobDetailsPanel jobDetailsPanel;
    private JobService jobService;

    public BrowseJobsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Browse Jobs");
        frame.setBounds(100, 100, 1200, 800);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Initialize service
        jobService = new JobService(frame);

        // Top Panel with Back Button and Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.setVisible(false); // Hide first
            frame.dispose(); // Then dispose
            SwingUtilities.invokeLater(() -> MainPage.getInstance().show()); // Show main page on EDT
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("Job Listings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(450));
        topPanel.add(titleLabel);

        // Initialize panels
        filtersPanel = new FiltersPanel(this::refreshJobList);
        jobsTablePanel = new JobsTablePanel(this::loadJobDetails);
        jobDetailsPanel = new JobDetailsPanel();

        // Layout setup
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Add components to main panel
        mainPanel.add(filtersPanel, BorderLayout.NORTH);
        mainPanel.add(jobsTablePanel, BorderLayout.CENTER);
        mainPanel.add(jobDetailsPanel, BorderLayout.SOUTH);

        // Add panels to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(mainPanel, BorderLayout.CENTER);

        // Initial data load
        refreshJobList();
    }

    private void refreshJobList() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            String query = jobService.buildQueryFromFilters(
                filtersPanel.getSearchText(),
                filtersPanel.getSelectedLocation(),
                filtersPanel.getSelectedSector(),
                filtersPanel.getSelectedContract(),
                filtersPanel.isRemoteOnly()
            );

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                jobService.setQueryParameters(pstmt,
                    filtersPanel.getSearchText(),
                    filtersPanel.getSelectedLocation(),
                    filtersPanel.getSelectedSector(),
                    filtersPanel.getSelectedContract()
                );

                ResultSet rs = pstmt.executeQuery();
                int count = 0;
                jobsTablePanel.clearTable();
                
                while (rs.next()) {
                    Vector<Object> row = jobService.createRowFromResultSet(rs);
                    jobsTablePanel.addRow(row);
                    count++;
                }
                
                filtersPanel.updateResultCount(count);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                "Error refreshing job list: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadJobDetails(String company, String title) {
        String details = jobService.getJobDetails(company, title);
        jobDetailsPanel.setDetails(details);
    }

    public void show() {
        SwingUtilities.invokeLater(() -> {
            frame.setVisible(true);
            frame.requestFocus();
        });
    }
}
