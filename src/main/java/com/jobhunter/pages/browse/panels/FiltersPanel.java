package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.jobhunter.util.DatabaseConnection;

public class FiltersPanel extends JPanel {
    private JTextField searchField;
    private JComboBox<String> locationFilter;
    private JComboBox<String> sectorFilter;
    private JComboBox<String> contractFilter;
    private JCheckBox remoteFilter;
    private JLabel resultCount;
    private Runnable onFilterChange;

    public FiltersPanel(Runnable onFilterChange) {
        this.onFilterChange = onFilterChange;
        initialize();
        initializeFilters();
    }

    private void initialize() {
        setLayout(new FlowLayout(FlowLayout.LEFT, 10, 5));
        setBorder(BorderFactory.createTitledBorder("Filters"));

        // Search field
        searchField = new JTextField(20);
        searchField.addActionListener(e -> onFilterChange.run());
        add(new JLabel("Search:"));
        add(searchField);

        // Location filter
        locationFilter = new JComboBox<>();
        add(new JLabel("Location:"));
        add(locationFilter);

        // Sector filter
        sectorFilter = new JComboBox<>();
        add(new JLabel("Sector:"));
        add(sectorFilter);

        // Contract type filter
        contractFilter = new JComboBox<>();
        add(new JLabel("Contract:"));
        add(contractFilter);

        // Remote work filter
        remoteFilter = new JCheckBox("Remote Only");
        add(remoteFilter);

        // Apply filters button
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> onFilterChange.run());
        add(applyButton);

        // Results count
        resultCount = new JLabel("0 jobs found");
        add(Box.createHorizontalStrut(20));
        add(resultCount);
    }

    private void initializeFilters() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Load locations
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT location FROM job_post WHERE location IS NOT NULL ORDER BY location")) {
                locationFilter.addItem("All Locations");
                while (rs.next()) {
                    locationFilter.addItem(rs.getString("location"));
                }
            }

            // Load sectors
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT sector FROM job_post WHERE sector IS NOT NULL ORDER BY sector")) {
                sectorFilter.addItem("All Sectors");
                while (rs.next()) {
                    sectorFilter.addItem(rs.getString("sector"));
                }
            }

            // Load contract types
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(
                    "SELECT DISTINCT contract_type FROM job_post WHERE contract_type IS NOT NULL ORDER BY contract_type")) {
                contractFilter.addItem("All Contracts");
                while (rs.next()) {
                    contractFilter.addItem(rs.getString("contract_type"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading filters: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    public String getSearchText() {
        return searchField.getText().trim();
    }

    public String getSelectedLocation() {
        return (String) locationFilter.getSelectedItem();
    }

    public String getSelectedSector() {
        return (String) sectorFilter.getSelectedItem();
    }

    public String getSelectedContract() {
        return (String) contractFilter.getSelectedItem();
    }

    public boolean isRemoteOnly() {
        return remoteFilter.isSelected();
    }

    public void updateResultCount(int count) {
        resultCount.setText(count + " jobs found");
    }
}
