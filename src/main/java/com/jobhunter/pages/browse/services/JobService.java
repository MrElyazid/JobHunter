package com.jobhunter.pages.browse.services;

import java.sql.*;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.jobhunter.util.DatabaseConnection;

public class JobService {
    private JFrame parentFrame;

    public JobService(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public String buildQueryFromFilters(String searchText, String location, String sector, 
                                      String contract, boolean remoteOnly) {
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT * FROM job_post WHERE 1=1"
        );

        if (!searchText.isEmpty()) {
            queryBuilder.append(" AND (job_description LIKE ? OR company LIKE ?)");
        }
        
        if (location != null && !location.equals("All Locations")) {
            queryBuilder.append(" AND location = ?");
        }

        if (sector != null && !sector.equals("All Sectors")) {
            queryBuilder.append(" AND sector = ?");
        }

        if (contract != null && !contract.equals("All Contracts")) {
            queryBuilder.append(" AND contract_type = ?");
        }

        if (remoteOnly) {
            queryBuilder.append(" AND is_remote = true");
        }

        queryBuilder.append(" ORDER BY company, min_salary DESC");
        return queryBuilder.toString();
    }

    public void setQueryParameters(PreparedStatement pstmt, String searchText, String location, 
                                 String sector, String contract) throws SQLException {
        int paramIndex = 1;
        if (!searchText.isEmpty()) {
            String searchPattern = "%" + searchText + "%";
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
    }

    public Vector<Object> createRowFromResultSet(ResultSet rs) throws SQLException {
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
        return row;
    }

    public String getJobDetails(String company, String title) {
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
                
                return details.toString();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(parentFrame,
                "Error loading job details: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
        return "Error loading job details";
    }
}
