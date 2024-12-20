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
        frame.setLayout(new BorderLayout(0, 0));

        // Initialize service
        jobService = new JobService(frame);

        // Top Panel with Back Button and Title
        JPanel topPanel = createTopPanel();
        
        // Initialize panels
        filtersPanel = new FiltersPanel(this::refreshJobList);
        jobsTablePanel = new JobsTablePanel(this::loadJobDetails);
        jobDetailsPanel = new JobDetailsPanel();

        // Create main split pane for jobs table and details
        JSplitPane mainSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        mainSplitPane.setTopComponent(jobsTablePanel);
        mainSplitPane.setBottomComponent(jobDetailsPanel);
        mainSplitPane.setResizeWeight(0.7); // Give 70% to table by default
        mainSplitPane.setOneTouchExpandable(true);
        
        // Create content panel that holds filters and main split pane
        JPanel contentPanel = new JPanel(new BorderLayout(0, 5));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        contentPanel.add(filtersPanel, BorderLayout.NORTH);
        contentPanel.add(mainSplitPane, BorderLayout.CENTER);

        // Add panels to frame
        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(contentPanel, BorderLayout.CENTER);

        // Initial data load
        refreshJobList();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();
        topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.X_AXIS));
        topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // Back button
        JButton backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.setVisible(false);
            frame.dispose();
            SwingUtilities.invokeLater(() -> MainPage.getInstance().show());
        });

        // Title
        JLabel titleLabel = new JLabel("Job Listings");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Add components with proper spacing
        topPanel.add(backButton);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(titleLabel);
        topPanel.add(Box.createHorizontalGlue());
        topPanel.add(Box.createHorizontalStrut(backButton.getPreferredSize().width)); // Balance the back button

        return topPanel;
    }

    private void refreshJobList() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Build query with all filter parameters
            String query = jobService.buildQueryFromFilters(
                filtersPanel.getSearchText(),
                filtersPanel.getSelectedLocation(),
                filtersPanel.getSelectedRegion(),
                filtersPanel.getSelectedSector(),
                filtersPanel.getSelectedContract(),
                filtersPanel.getSelectedLanguage(),
                filtersPanel.getSelectedProficiency(),
                filtersPanel.getSelectedExperience(),
                filtersPanel.getMinSalary(),
                filtersPanel.getMaxSalary(),
                filtersPanel.getPublicationDateFilter(),
                filtersPanel.isRemoteOnly(),
                filtersPanel.isForeignCompanyOnly(),
                filtersPanel.isInternshipOnly()
            );

            try (PreparedStatement pstmt = conn.prepareStatement(query)) {
                // Set all query parameters
                jobService.setQueryParameters(pstmt,
                    filtersPanel.getSearchText(),
                    filtersPanel.getSelectedLocation(),
                    filtersPanel.getSelectedRegion(),
                    filtersPanel.getSelectedSector(),
                    filtersPanel.getSelectedContract(),
                    filtersPanel.getSelectedLanguage(),
                    filtersPanel.getSelectedProficiency(),
                    filtersPanel.getMinSalary(),
                    filtersPanel.getMaxSalary(),
                    filtersPanel.getPublicationDateFilter()
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
