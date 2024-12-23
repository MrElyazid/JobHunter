package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
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
            BorderFactory.createTitledBorder(null, "Filters",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 118, 210)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        // Create main filter panel with GridBagLayout
        JPanel mainFilterPanel = new JPanel(new GridBagLayout());
        mainFilterPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Search Panel (Top)
        JPanel searchPanel = new JPanel(new BorderLayout(8, 0));
        searchPanel.setBackground(Color.WHITE);
        searchField = new JTextField();
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        searchField.addActionListener(e -> onFilterChange.run());
        
        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        searchLabel.setForeground(new Color(66, 66, 66));
        
        searchPanel.add(searchLabel, BorderLayout.WEST);
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
        
        // Salary panel with modern styling
        JPanel salaryPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        salaryPanel.setBackground(Color.WHITE);
        minSalarySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1000));
        maxSalarySpinner = new JSpinner(new SpinnerNumberModel(0, 0, 100000, 1000));
        
        JLabel salaryLabel = new JLabel("Salary:");
        salaryLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        salaryLabel.setForeground(new Color(66, 66, 66));
        
        // Style spinners
        JComponent minEditor = minSalarySpinner.getEditor();
        JFormattedTextField minField = ((JSpinner.DefaultEditor) minEditor).getTextField();
        minField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        JComponent maxEditor = maxSalarySpinner.getEditor();
        JFormattedTextField maxField = ((JSpinner.DefaultEditor) maxEditor).getTextField();
        maxField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        
        salaryPanel.add(salaryLabel);
        salaryPanel.add(minSalarySpinner);
        salaryPanel.add(new JLabel("-"));
        salaryPanel.add(maxSalarySpinner);

        gbc.gridx = 0;
        mainFilterPanel.add(experiencePanel, gbc);
        gbc.gridx = 1;
        mainFilterPanel.add(salaryPanel, gbc);

        // Bottom panel with checkboxes and buttons
        JPanel bottomPanel = new JPanel(new BorderLayout(8, 0));
        bottomPanel.setBackground(Color.WHITE);
        
        // Checkbox panel with modern styling
        JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        checkboxPanel.setBackground(Color.WHITE);
        
        // Style checkboxes
        remoteFilter = createStyledCheckbox("Remote Only");
        foreignCompanyFilter = createStyledCheckbox("Foreign Companies");
        internshipFilter = createStyledCheckbox("Internships");
        
        // Style publication date filter
        JLabel publishedLabel = new JLabel("Published:");
        publishedLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        publishedLabel.setForeground(new Color(66, 66, 66));
        
        publicationDateFilter = new JComboBox<>(new String[]{
            "All Time", "Today", "Last 3 Days", "Last Week", "Last Month"
        });
        publicationDateFilter.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        publicationDateFilter.setBackground(Color.WHITE);
        
        checkboxPanel.add(remoteFilter);
        checkboxPanel.add(foreignCompanyFilter);
        checkboxPanel.add(internshipFilter);
        checkboxPanel.add(publishedLabel);
        checkboxPanel.add(publicationDateFilter);
        
        // Button and count panel with modern styling
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 0));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton applyButton = new JButton("Apply Filters");
        applyButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        applyButton.setForeground(Color.WHITE);
        applyButton.setBackground(new Color(25, 118, 210));
        applyButton.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        applyButton.setFocusPainted(false);
        applyButton.addActionListener(e -> onFilterChange.run());
        
        // Add hover effect to button
        applyButton.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                applyButton.setBackground(new Color(21, 101, 192));
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                applyButton.setBackground(new Color(25, 118, 210));
            }
        });
        
        resultCount = new JLabel("0 jobs found");
        resultCount.setFont(new Font("Segoe UI", Font.BOLD, 13));
        resultCount.setForeground(new Color(66, 66, 66));
        
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
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setBackground(Color.WHITE);
        
        JLabel filterLabel = new JLabel(label);
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 13));
        filterLabel.setForeground(new Color(66, 66, 66));
        
        comboBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        comboBox.setBackground(Color.WHITE);
        comboBox.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        
        panel.add(filterLabel, BorderLayout.WEST);
        panel.add(comboBox, BorderLayout.CENTER);
        return panel;
    }

    private JCheckBox createStyledCheckbox(String text) {
        JCheckBox checkbox = new JCheckBox(text);
        checkbox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        checkbox.setForeground(new Color(66, 66, 66));
        checkbox.setBackground(Color.WHITE);
        return checkbox;
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
