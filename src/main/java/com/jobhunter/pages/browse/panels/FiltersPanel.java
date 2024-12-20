package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import com.jobhunter.util.DatabaseConnection;

public class FiltersPanel extends JPanel {
    private JTextField searchField;
    private JComboBox<String> locationFilter;
    private JComboBox<String> regionFilter;
    private JComboBox<String> sectorFilter;
    private JComboBox<String> contractFilter;
    private JComboBox<String> languageFilter;
    private JComboBox<String> proficiencyFilter;
    private JComboBox<String> experienceFilter;
    private JSpinner minSalarySpinner;
    private JSpinner maxSalarySpinner;
    private JComboBox<String> publicationDateFilter;
    private JCheckBox remoteFilter;
    private JCheckBox foreignCompanyFilter;
    private JCheckBox internshipFilter;
    private JLabel resultCount;
    private Runnable onFilterChange;

    public FiltersPanel(Runnable onFilterChange) {
        this.onFilterChange = onFilterChange;
        initialize();
        initializeFilters();
    }

    private void initialize() {
        setLayout(new BorderLayout(5, 5));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Filters"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        // Create main filter panel with GridBagLayout
        JPanel mainFilterPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 2, 2, 2);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Panel (Top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchField = new JTextField();
        searchField.addActionListener(e -> onFilterChange.run());
        searchPanel.add(new JLabel("Search:"), BorderLayout.WEST);
        searchPanel.add(searchField, BorderLayout.CENTER);

        // First row of filters
        gbc.gridy = 0;
        gbc.gridwidth = 1;
        gbc.weightx = 1.0;

        // Location and Region (same row)
        JPanel locationPanel = createFilterPanel("Location:", locationFilter = new JComboBox<>());
        JPanel regionPanel = createFilterPanel("Region:", regionFilter = new JComboBox<>());
        gbc.gridx = 0;
        mainFilterPanel.add(locationPanel, gbc);
        gbc.gridx = 1;
        mainFilterPanel.add(regionPanel, gbc);

        // Second row
        gbc.gridy = 1;
        JPanel sectorPanel = createFilterPanel("Sector:", sectorFilter = new JComboBox<>());
        JPanel contractPanel = createFilterPanel("Contract:", contractFilter = new JComboBox<>());
        gbc.gridx = 0;
        mainFilterPanel.add(sectorPanel, gbc);
        gbc.gridx = 1;
        mainFilterPanel.add(contractPanel, gbc);

        // Third row
        gbc.gridy = 2;
        JPanel languagePanel = createFilterPanel("Language:", languageFilter = new JComboBox<>());
        proficiencyFilter = new JComboBox<>(new String[]{"All", "Basic", "Intermediate", "Advanced", "Fluent"});
        JPanel proficiencyPanel = createFilterPanel("Proficiency:", proficiencyFilter);
        gbc.gridx = 0;
        mainFilterPanel.add(languagePanel, gbc);
        gbc.gridx = 1;
        mainFilterPanel.add(proficiencyPanel, gbc);

        // Fourth row
        gbc.gridy = 3;
        experienceFilter = new JComboBox<>(new String[]{"All", "Entry Level", "1-3 years", "3-5 years", "5+ years"});
        JPanel experiencePanel = createFilterPanel("Experience:", experienceFilter);
        
        // Salary panel
        JPanel salaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        minSalarySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1000));
        maxSalarySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1000));
        salaryPanel.add(new JLabel("Salary:"));
        salaryPanel.add(minSalarySpinner);
        salaryPanel.add(new JLabel("-"));
        salaryPanel.add(maxSalarySpinner);

        gbc.gridx = 0;
        mainFilterPanel.add(experiencePanel, gbc);
        gbc.gridx = 1;
        mainFilterPanel.add(salaryPanel, gbc);

        // Bottom panel with checkboxes and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(5, 0));
        
        // Checkbox panel
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        remoteFilter = new JCheckBox("Remote Only");
        foreignCompanyFilter = new JCheckBox("Foreign Companies");
        internshipFilter = new JCheckBox("Internships");
        publicationDateFilter = new JComboBox<>(new String[]{
            "All Time", "Today", "Last 3 Days", "Last Week", "Last Month"
        });
        
        checkboxPanel.add(remoteFilter);
        checkboxPanel.add(foreignCompanyFilter);
        checkboxPanel.add(internshipFilter);
        checkboxPanel.add(new JLabel("Published:"));
        checkboxPanel.add(publicationDateFilter);
        
        // Button and count panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> onFilterChange.run());
        resultCount = new JLabel("0 jobs found");
        buttonPanel.add(resultCount);
        buttonPanel.add(applyButton);

        bottomPanel.add(checkboxPanel, BorderLayout.WEST);
        bottomPanel.add(buttonPanel, BorderLayout.EAST);

        // Add all components to the main panel
        add(searchPanel, BorderLayout.NORTH);
        add(mainFilterPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);
    }

    private JPanel createFilterPanel(String label, JComboBox<String> comboBox) {
        JPanel panel = new JPanel(new BorderLayout(5, 0));
        panel.add(new JLabel(label), BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    private void initializeFilters() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            loadDistinctValues(conn, "location", locationFilter);
            loadDistinctValues(conn, "region", regionFilter);
            loadDistinctValues(conn, "sector", sectorFilter);
            loadDistinctValues(conn, "contract_type", contractFilter);
            loadDistinctValues(conn, "languages", languageFilter);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, 
                "Error loading filters: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadDistinctValues(Connection conn, String columnName, JComboBox<String> comboBox) throws SQLException {
        String query = "SELECT DISTINCT " + columnName + " FROM job_post WHERE " + columnName + " IS NOT NULL ORDER BY " + columnName;
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            comboBox.addItem("All");
            while (rs.next()) {
                String value = rs.getString(1);
                if (value != null && !value.trim().isEmpty()) {
                    comboBox.addItem(value.trim());
                }
            }
        }
    }

    // Getter methods
    public String getSearchText() { return searchField.getText().trim(); }
    public String getSelectedLocation() { return getSelectedValue(locationFilter); }
    public String getSelectedRegion() { return getSelectedValue(regionFilter); }
    public String getSelectedSector() { return getSelectedValue(sectorFilter); }
    public String getSelectedContract() { return getSelectedValue(contractFilter); }
    public String getSelectedLanguage() { return getSelectedValue(languageFilter); }
    public String getSelectedProficiency() { return getSelectedValue(proficiencyFilter); }
    public String getSelectedExperience() { return getSelectedValue(experienceFilter); }
    public int getMinSalary() { return (Integer) minSalarySpinner.getValue(); }
    public int getMaxSalary() { return (Integer) maxSalarySpinner.getValue(); }
    public boolean isRemoteOnly() { return remoteFilter.isSelected(); }
    public boolean isForeignCompanyOnly() { return foreignCompanyFilter.isSelected(); }
    public boolean isInternshipOnly() { return internshipFilter.isSelected(); }

    public LocalDate getPublicationDateFilter() {
        String selected = (String) publicationDateFilter.getSelectedItem();
        LocalDate now = LocalDate.now();
        
        switch (selected) {
            case "Today": return now;
            case "Last 3 Days": return now.minusDays(3);
            case "Last Week": return now.minusWeeks(1);
            case "Last Month": return now.minusMonths(1);
            default: return null;
        }
    }

    private String getSelectedValue(JComboBox<String> comboBox) {
        String value = (String) comboBox.getSelectedItem();
        return "All".equals(value) ? null : value;
    }

    public void updateResultCount(int count) {
        resultCount.setText(count + " jobs found");
    }
}
