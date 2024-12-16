package com.jobhunter.pages.browse;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import com.jobhunter.util.DatabaseConnection;
import java.util.Vector;

public class BrowseJobsPage {
    private JFrame frame;
    private JTable jobsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> locationFilter;
    private JComboBox<String> sectorFilter;
    private JComboBox<String> contractFilter;
    private JCheckBox remoteFilter;
    private JButton backButton;
    private JLabel resultCount;

    public BrowseJobsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Browse Jobs");
        frame.setBounds(100, 100, 1200, 800);
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
        
        JLabel titleLabel = new JLabel("Job Listings", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(450));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Filters Panel
        JPanel filtersPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filtersPanel.setBorder(BorderFactory.createTitledBorder("Filters"));

        // Search field
        searchField = new JTextField(20);
        searchField.addActionListener(e -> refreshJobList());
        filtersPanel.add(new JLabel("Search:"));
        filtersPanel.add(searchField);

        // Location filter
        locationFilter = new JComboBox<>();
        filtersPanel.add(new JLabel("Location:"));
        filtersPanel.add(locationFilter);

        // Sector filter
        sectorFilter = new JComboBox<>();
        filtersPanel.add(new JLabel("Sector:"));
        filtersPanel.add(sectorFilter);

        // Contract type filter
        contractFilter = new JComboBox<>();
        filtersPanel.add(new JLabel("Contract:"));
        filtersPanel.add(contractFilter);

        // Remote work filter
        remoteFilter = new JCheckBox("Remote Only");
        filtersPanel.add(remoteFilter);

        // Apply filters button
        JButton applyButton = new JButton("Apply Filters");
        applyButton.addActionListener(e -> refreshJobList());
        filtersPanel.add(applyButton);

        // Results count
        resultCount = new JLabel("0 jobs found");
        filtersPanel.add(Box.createHorizontalStrut(20));
        filtersPanel.add(resultCount);

        frame.add(filtersPanel, BorderLayout.NORTH);

        // Table
        String[] columnNames = {
            "Title", "Company", "Location", "Salary", "Contract", "Experience", 
            "Remote", "Sector", "Required Skills"
        };
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        jobsTable = new JTable(tableModel);
        jobsTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        jobsTable.getColumnModel().getColumn(0).setPreferredWidth(200); // Title
        jobsTable.getColumnModel().getColumn(1).setPreferredWidth(150); // Company
        jobsTable.getColumnModel().getColumn(2).setPreferredWidth(100); // Location
        jobsTable.getColumnModel().getColumn(3).setPreferredWidth(100); // Salary
        jobsTable.getColumnModel().getColumn(4).setPreferredWidth(100); // Contract
        jobsTable.getColumnModel().getColumn(5).setPreferredWidth(100); // Experience
        jobsTable.getColumnModel().getColumn(6).setPreferredWidth(80);  // Remote
        jobsTable.getColumnModel().getColumn(7).setPreferredWidth(150); // Sector
        jobsTable.getColumnModel().getColumn(8).setPreferredWidth(200); // Skills

        JScrollPane scrollPane = new JScrollPane(jobsTable);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Job Details Panel
        JPanel detailsPanel = new JPanel();
        detailsPanel.setPreferredSize(new Dimension(frame.getWidth(), 200));
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Job Details"));
        detailsPanel.setLayout(new BorderLayout());

        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setLineWrap(true);
        detailsArea.setMargin(new Insets(5, 5, 5, 5));

        jobsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && jobsTable.getSelectedRow() != -1) {
                int row = jobsTable.getSelectedRow();
                String company = (String) jobsTable.getValueAt(row, 1);
                String title = (String) jobsTable.getValueAt(row, 0);
                loadJobDetails(detailsArea, company, title);
            }
        });

        detailsPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
        frame.add(detailsPanel, BorderLayout.SOUTH);

        // Initialize filters and load data
        initializeFilters();
        refreshJobList();
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
            JOptionPane.showMessageDialog(frame, 
                "Error loading filters: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void refreshJobList() {
        tableModel.setRowCount(0);
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT * FROM job_post WHERE 1=1"
        );

        // Apply filters
        if (!searchField.getText().isEmpty()) {
            queryBuilder.append(" AND (job_description LIKE ? OR company LIKE ?)");
        }
        
        String location = (String) locationFilter.getSelectedItem();
        if (location != null && !location.equals("All Locations")) {
            queryBuilder.append(" AND location = ?");
        }

        String sector = (String) sectorFilter.getSelectedItem();
        if (sector != null && !sector.equals("All Sectors")) {
            queryBuilder.append(" AND sector = ?");
        }

        String contract = (String) contractFilter.getSelectedItem();
        if (contract != null && !contract.equals("All Contracts")) {
            queryBuilder.append(" AND contract_type = ?");
        }

        if (remoteFilter.isSelected()) {
            queryBuilder.append(" AND is_remote = true");
        }

        queryBuilder.append(" ORDER BY company, min_salary DESC");

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(queryBuilder.toString())) {
            
            int paramIndex = 1;
            if (!searchField.getText().isEmpty()) {
                String searchPattern = "%" + searchField.getText() + "%";
                pstmt.setString(paramIndex++, searchPattern);
                pstmt.setString(paramIndex++, searchPattern);
            }
            
            if (location != null && !location.equals("All Locations")) {
                pstmt.setString(paramIndex++, location);
            }
            
            if (sector != null && !sector.equals("All Sectors")) {
                pstmt.setString(paramIndex++, sector);
            }
            
            if (contract != null && !contract.equals("All Contracts")) {
                pstmt.setString(paramIndex++, contract);
            }

            ResultSet rs = pstmt.executeQuery();
            int count = 0;
            while (rs.next()) {
                Vector<Object> row = new Vector<>();
                row.add(rs.getString("job_description"));
                row.add(rs.getString("company"));
                row.add(rs.getString("location"));
                row.add(rs.getDouble("min_salary"));
                row.add(rs.getString("contract_type"));
                row.add(rs.getInt("min_experience") + " years");
                row.add(rs.getBoolean("is_remote") ? "Yes" : "No");
                row.add(rs.getString("sector"));
                row.add(rs.getString("hard_skills"));
                tableModel.addRow(row);
                count++;
            }
            resultCount.setText(count + " jobs found");

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame,
                "Error refreshing job list: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void loadJobDetails(JTextArea detailsArea, String company, String title) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM job_post WHERE company = ? AND job_description = ?")) {
            
            pstmt.setString(1, company);
            pstmt.setString(2, title);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Company: ").append(company).append("\n");
                details.append("Position: ").append(title).append("\n\n");
                details.append("Company Description:\n").append(rs.getString("company_description")).append("\n\n");
                details.append("Required Skills:\n").append(rs.getString("hard_skills")).append("\n\n");
                details.append("Soft Skills:\n").append(rs.getString("soft_skills")).append("\n");
                
                detailsArea.setText(details.toString());
                detailsArea.setCaretPosition(0);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            detailsArea.setText("Error loading job details: " + e.getMessage());
        }
    }

    public void show() {
        frame.setVisible(true);
    }
}
