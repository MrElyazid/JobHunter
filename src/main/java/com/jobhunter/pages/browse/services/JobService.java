package com.jobhunter.pages.browse.services;

import java.sql.*;
import java.time.LocalDate;
import java.util.Vector;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.jobhunter.util.DatabaseConnection;

public class JobService {
    private JFrame parentFrame;

    public JobService(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public String buildQueryFromFilters(String searchText, String location, String region,
                                      String sector, String contract, String language,
                                      String proficiency, String experience,
                                      int minSalary, int maxSalary, LocalDate publicationDate,
                                      boolean remoteOnly, boolean foreignCompanyOnly,
                                      boolean internshipOnly) {
        StringBuilder queryBuilder = new StringBuilder(
            "SELECT * FROM job_post WHERE 1=1"
        );

        // Text search across multiple fields
        if (!searchText.isEmpty()) {
            queryBuilder.append(" AND (title LIKE ? OR job_description LIKE ? OR " +
                              "company LIKE ? OR hard_skills LIKE ? OR soft_skills LIKE ?)");
        }
        
        // Location and region filters
        if (location != null) {
            queryBuilder.append(" AND location = ?");
        }
        if (region != null) {
            queryBuilder.append(" AND region = ?");
        }

        // Sector and contract filters
        if (sector != null) {
            queryBuilder.append(" AND sector = ?");
        }
        if (contract != null) {
            queryBuilder.append(" AND contract_type = ?");
        }

        // Language and proficiency filters
        if (language != null) {
            queryBuilder.append(" AND languages LIKE ?");
        }
        if (proficiency != null) {
            queryBuilder.append(" AND language_profeciency = ?");
        }

        // Experience filter
        if (experience != null) {
            switch (experience) {
                case "Entry Level":
                    queryBuilder.append(" AND min_experience <= 1");
                    break;
                case "1-3 years":
                    queryBuilder.append(" AND min_experience BETWEEN 1 AND 3");
                    break;
                case "3-5 years":
                    queryBuilder.append(" AND min_experience BETWEEN 3 AND 5");
                    break;
                case "5+ years":
                    queryBuilder.append(" AND min_experience > 5");
                    break;
            }
        }

        // Salary range filter
        if (minSalary > 0) {
            queryBuilder.append(" AND min_salary >= ?");
        }
        if (maxSalary > 0) {
            queryBuilder.append(" AND min_salary <= ?");
        }

        // Publication date filter
        if (publicationDate != null) {
            queryBuilder.append(" AND date_of_publication >= ?");
        }

        // Boolean filters
        if (remoteOnly) {
            queryBuilder.append(" AND is_remote = true");
        }
        if (foreignCompanyOnly) {
            queryBuilder.append(" AND foriegn_company = true");
        }
        if (internshipOnly) {
            queryBuilder.append(" AND is_internship = true");
        }

        queryBuilder.append(" ORDER BY date_of_publication DESC, min_salary DESC");
        return queryBuilder.toString();
    }

    public void setQueryParameters(PreparedStatement pstmt, String searchText, String location,
                                 String region, String sector, String contract, String language,
                                 String proficiency, int minSalary, int maxSalary,
                                 LocalDate publicationDate) throws SQLException {
        int paramIndex = 1;

        // Set search text parameters
        if (!searchText.isEmpty()) {
            String searchPattern = "%" + searchText + "%";
            for (int i = 0; i < 5; i++) { // 5 fields to search in
                pstmt.setString(paramIndex++, searchPattern);
            }
        }
        
        // Set location and region
        if (location != null) {
            pstmt.setString(paramIndex++, location);
        }
        if (region != null) {
            pstmt.setString(paramIndex++, region);
        }
        
        // Set sector and contract
        if (sector != null) {
            pstmt.setString(paramIndex++, sector);
        }
        if (contract != null) {
            pstmt.setString(paramIndex++, contract);
        }
        
        // Set language and proficiency
        if (language != null) {
            pstmt.setString(paramIndex++, "%" + language + "%");
        }
        if (proficiency != null) {
            pstmt.setString(paramIndex++, proficiency);
        }
        
        // Set salary range
        if (minSalary > 0) {
            pstmt.setInt(paramIndex++, minSalary);
        }
        if (maxSalary > 0) {
            pstmt.setInt(paramIndex++, maxSalary);
        }
        
        // Set publication date
        if (publicationDate != null) {
            pstmt.setDate(paramIndex++, Date.valueOf(publicationDate));
        }
    }

    public Vector<Object> createRowFromResultSet(ResultSet rs) throws SQLException {
        Vector<Object> row = new Vector<>();
        row.add(rs.getString("title"));
        row.add(rs.getString("company"));
        row.add(rs.getString("location"));
        row.add(rs.getString("region"));
        row.add(rs.getString("sector"));
        row.add(rs.getString("job"));
        row.add(rs.getDouble("min_salary"));
        row.add(rs.getString("contract_type"));
        row.add(rs.getInt("min_experience"));
        row.add(rs.getBoolean("is_remote"));
        row.add(rs.getBoolean("foriegn_company"));
        row.add(rs.getBoolean("is_internship"));
        row.add(rs.getString("source"));
        row.add(rs.getString("link"));
        row.add(rs.getDate("application_date"));
        row.add(rs.getDate("date_of_publication"));
        row.add(rs.getString("company_address"));
        row.add(rs.getString("company_website"));
        row.add(rs.getString("hard_skills"));
        row.add(rs.getString("soft_skills"));
        row.add(rs.getString("diploma"));
        row.add(rs.getString("desired_profile"));
        row.add(rs.getString("personality_traits"));
        row.add(rs.getString("languages"));
        row.add(rs.getString("language_profeciency"));
        row.add(rs.getString("recommended_skills"));
        return row;
    }

    public String getJobDetails(String company, String title) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(
                "SELECT * FROM job_post WHERE company = ? AND title = ?")) {
            
            pstmt.setString(1, company);
            pstmt.setString(2, title);
            
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                StringBuilder details = new StringBuilder();
                details.append("Job Title: ").append(title).append("\n\n");
                details.append("Company: ").append(company).append("\n");
                details.append("Location: ").append(rs.getString("location"))
                      .append(" (").append(rs.getString("region")).append(")\n");
                details.append("Company Website: ").append(rs.getString("company_website")).append("\n");
                details.append("Company Address: ").append(rs.getString("company_address")).append("\n\n");
                
                details.append("Job Details:\n");
                details.append("• Contract Type: ").append(rs.getString("contract_type")).append("\n");
                details.append("• Minimum Salary: ").append(rs.getDouble("min_salary")).append(" DH\n");
                details.append("• Experience Required: ").append(rs.getInt("min_experience")).append(" years\n");
                details.append("• Remote Work: ").append(rs.getBoolean("is_remote") ? "Yes" : "No").append("\n");
                details.append("• Internship: ").append(rs.getBoolean("is_internship") ? "Yes" : "No").append("\n\n");
                
                details.append("Required Languages:\n");
                details.append(rs.getString("languages")).append(" (")
                       .append(rs.getString("language_profeciency")).append(")\n\n");
                
                details.append("Company Description:\n");
                details.append(rs.getString("company_description")).append("\n\n");
                
                details.append("Job Description:\n");
                details.append(rs.getString("job_description")).append("\n\n");
                
                details.append("Desired Profile:\n");
                details.append(rs.getString("desired_profile")).append("\n\n");
                
                details.append("Required Skills:\n");
                details.append("• Hard Skills: ").append(rs.getString("hard_skills")).append("\n");
                details.append("• Soft Skills: ").append(rs.getString("soft_skills")).append("\n");
                details.append("• Recommended Skills: ").append(rs.getString("recommended_skills")).append("\n\n");
                
                details.append("Required Qualifications:\n");
                details.append("• Diploma: ").append(rs.getString("diploma")).append("\n");
                details.append("• Personality Traits: ").append(rs.getString("personality_traits")).append("\n\n");
                
                details.append("Important Dates:\n");
                details.append("• Published: ").append(rs.getDate("date_of_publication")).append("\n");
                details.append("• Application Deadline: ").append(rs.getDate("application_date")).append("\n\n");
                
                details.append("Source: ").append(rs.getString("source")).append("\n");
                details.append("Apply at: ").append(rs.getString("link"));
                
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
