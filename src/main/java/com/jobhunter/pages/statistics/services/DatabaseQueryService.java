package com.jobhunter.pages.statistics.services;

import com.jobhunter.util.DatabaseConnection;
import java.sql.*;

public class DatabaseQueryService {
    private static Connection getConnection() throws SQLException {
        return DatabaseConnection.getConnection();
    }

    public static String getTotalJobCount() {
        try (Connection conn = getConnection();
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

    public static String getAverageSalary() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT AVG(min_salary) " +
                 "FROM job_post " +
                 "WHERE min_salary > 0 AND min_salary < 100000")) { // Filter unrealistic values
            if (rs.next()) {
                double avg = rs.getDouble(1);
                if (!rs.wasNull() && avg > 0) {
                    return String.format("%,.0f MAD", avg);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public static String getMostCommonContract() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT COALESCE(contract_type, 'Not Specified') as contract_type, COUNT(*) as count " +
                 "FROM job_post " +
                 "WHERE contract_type IS NOT NULL " +
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

    public static String getRemoteJobsPercentage() {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(
                 "SELECT (COUNT(CASE WHEN is_remote = true THEN 1 END) * 100.0 / " +
                 "COUNT(CASE WHEN is_remote IS NOT NULL THEN 1 END)) as remote_percent " +
                 "FROM job_post " +
                 "WHERE is_remote IS NOT NULL")) {
            if (rs.next()) {
                double percent = rs.getDouble(1);
                if (!rs.wasNull()) {
                    return String.format("%.1f%%", percent);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    public static ResultSet getContractTypeDistribution() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT COALESCE(contract_type, 'Not Specified') as contract_type, COUNT(*) as count " +
            "FROM job_post " +
            "GROUP BY contract_type " +
            "HAVING count > 5 " + // Filter out rare contract types
            "ORDER BY count DESC"
        );
    }

    public static ResultSet getCompanyTypeDistribution() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT COALESCE(foriegn_company, false) as foriegn_company, COUNT(*) as count " +
            "FROM job_post " +
            "WHERE company IS NOT NULL " + // Only count jobs with known companies
            "GROUP BY foriegn_company"
        );
    }

    public static ResultSet getJobsByCity() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT COALESCE(location, 'Other') as location, COUNT(*) as count " +
            "FROM job_post " +
            "WHERE location IS NOT NULL " +
            "AND location != 'Unknown' " +
            "AND location != '' " +
            "GROUP BY location " +
            "HAVING count >= 5 " + // Only show cities with significant job counts
            "ORDER BY count DESC " +
            "LIMIT 10"
        );
    }

    public static ResultSet getTopSkills() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "WITH RECURSIVE extracted_skills AS ( " +
            "  SELECT JSON_UNQUOTE(skill) as skill " +
            "  FROM job_post " +
            "  CROSS JOIN JSON_TABLE( " +
            "    CASE " +
            "      WHEN hard_skills != '[]' AND hard_skills IS NOT NULL THEN hard_skills " +
            "      ELSE '[\"\"]' " +
            "    END, " +
            "    '$[*]' COLUMNS (skill VARCHAR(255) PATH '$') " +
            "  ) skills " +
            "  WHERE hard_skills != '[]' AND hard_skills IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT JSON_UNQUOTE(skill) as skill " +
            "  FROM job_post " +
            "  CROSS JOIN JSON_TABLE( " +
            "    CASE " +
            "      WHEN soft_skills != '[]' AND soft_skills IS NOT NULL THEN soft_skills " +
            "      ELSE '[\"\"]' " +
            "    END, " +
            "    '$[*]' COLUMNS (skill VARCHAR(255) PATH '$') " +
            "  ) skills " +
            "  WHERE soft_skills != '[]' AND soft_skills IS NOT NULL " +
            ") " +
            "SELECT skill, COUNT(*) as count " +
            "FROM extracted_skills " +
            "WHERE skill != '' " +
            "GROUP BY skill " +
            "HAVING count >= 3 " + // Only show skills that appear multiple times
            "ORDER BY count DESC " +
            "LIMIT 10"
        );
    }

    public static ResultSet getJobTrends() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT " +
            "  COALESCE(sector, 'Other') as sector, " +
            "  COUNT(*) as job_count, " +
            "  ROUND(COUNT(CASE WHEN is_remote = true THEN 1 END) * 100.0 / COUNT(*), 1) as remote_percentage " +
            "FROM job_post " +
            "WHERE sector IS NOT NULL " +
            "AND sector != '' " +
            "GROUP BY sector " +
            "HAVING job_count >= 5 " + // Only show sectors with significant job counts
            "ORDER BY job_count DESC " +
            "LIMIT 8" // Limit to top sectors for better visualization
        );
    }
}
