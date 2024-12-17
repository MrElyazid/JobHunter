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
             ResultSet rs = stmt.executeQuery("SELECT AVG(min_salary) FROM job_post WHERE min_salary > 0")) {
            if (rs.next()) {
                return String.format("%,.0f MAD", rs.getDouble(1));
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

    public static String getRemoteJobsPercentage() {
        try (Connection conn = getConnection();
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

    public static ResultSet getContractTypeDistribution() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT contract_type, COUNT(*) as count " +
            "FROM job_post " +
            "GROUP BY contract_type " +
            "ORDER BY count DESC"
        );
    }

    public static ResultSet getCompanyTypeDistribution() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT foriegn_company, COUNT(*) as count " +
            "FROM job_post " +
            "GROUP BY foriegn_company"
        );
    }

    public static ResultSet getSalaryDistribution() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
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
            "ORDER BY salary_range"
        );
    }

    public static ResultSet getSalaryByExperience() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT min_experience, AVG(min_salary) as avg_salary " +
            "FROM job_post " +
            "WHERE min_salary > 0 " +
            "GROUP BY min_experience " +
            "ORDER BY min_experience"
        );
    }

    public static ResultSet getJobsByCity() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT location, COUNT(*) as count " +
            "FROM job_post " +
            "GROUP BY location " +
            "ORDER BY count DESC " +
            "LIMIT 10"
        );
    }

    public static ResultSet getSalaryByLocation() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT location, AVG(min_salary) as avg_salary " +
            "FROM job_post " +
            "WHERE min_salary > 0 " +
            "GROUP BY location " +
            "HAVING COUNT(*) > 5 " +
            "ORDER BY avg_salary DESC " +
            "LIMIT 10"
        );
    }

    public static ResultSet getTopSkills() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT skill, COUNT(*) as count FROM (" +
            "  SELECT JSON_UNQUOTE(JSON_EXTRACT(hard_skills, '$[*]')) as skill FROM job_post " +
            "  WHERE hard_skills IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT JSON_UNQUOTE(JSON_EXTRACT(soft_skills, '$[*]')) as skill FROM job_post " +
            "  WHERE soft_skills IS NOT NULL" +
            ") skills " +
            "WHERE skill IS NOT NULL " +
            "GROUP BY skill " +
            "ORDER BY count DESC " +
            "LIMIT 15"
        );
    }

    public static ResultSet getSkillsBySector() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT " +
            "  sector, " +
            "  skill, " +
            "  COUNT(*) as count, " +
            "  ROUND(COUNT(*) * 100.0 / sector_total.total, 1) as percentage " +
            "FROM (" +
            "  SELECT " +
            "    sector, " +
            "    JSON_UNQUOTE(JSON_EXTRACT(hard_skills, '$[*]')) as skill " +
            "  FROM job_post " +
            "  WHERE sector IS NOT NULL AND hard_skills IS NOT NULL " +
            "  UNION ALL " +
            "  SELECT " +
            "    sector, " +
            "    JSON_UNQUOTE(JSON_EXTRACT(soft_skills, '$[*]')) as skill " +
            "  FROM job_post " +
            "  WHERE sector IS NOT NULL AND soft_skills IS NOT NULL " +
            ") skills " +
            "JOIN (" +
            "  SELECT sector, COUNT(*) as total " +
            "  FROM job_post " +
            "  WHERE sector IS NOT NULL " +
            "  GROUP BY sector" +
            ") sector_total ON skills.sector = sector_total.sector " +
            "WHERE skill IS NOT NULL " +
            "GROUP BY sector, skill " +
            "HAVING count >= 5 " +
            "ORDER BY sector, count DESC"
        );
    }

    public static ResultSet getTrendingSkills() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT " +
            "  skill, " +
            "  COUNT(*) as demand_count, " +
            "  ROUND(AVG(min_salary), 0) as avg_salary " +
            "FROM (" +
            "  SELECT " +
            "    JSON_UNQUOTE(JSON_EXTRACT(hard_skills, '$[*]')) as skill, " +
            "    min_salary " +
            "  FROM job_post " +
            "  WHERE hard_skills IS NOT NULL AND min_salary > 0 " +
            "  UNION ALL " +
            "  SELECT " +
            "    JSON_UNQUOTE(JSON_EXTRACT(soft_skills, '$[*]')) as skill, " +
            "    min_salary " +
            "  FROM job_post " +
            "  WHERE soft_skills IS NOT NULL AND min_salary > 0 " +
            ") skills " +
            "WHERE skill IS NOT NULL " +
            "GROUP BY skill " +
            "HAVING COUNT(*) >= 5 " +
            "ORDER BY (COUNT(*) * AVG(min_salary)) DESC " +
            "LIMIT 10"
        );
    }

    public static ResultSet getJobTrends() throws SQLException {
        Statement stmt = getConnection().createStatement();
        return stmt.executeQuery(
            "SELECT " +
            "  sector, " +
            "  COUNT(*) as job_count, " +
            "  ROUND(AVG(CASE WHEN min_salary > 0 THEN min_salary END), 0) as avg_salary, " +
            "  ROUND(COUNT(CASE WHEN is_remote = true THEN 1 END) * 100.0 / COUNT(*), 1) as remote_percentage, " +
            "  ROUND(COUNT(CASE WHEN foriegn_company = true THEN 1 END) * 100.0 / COUNT(*), 1) as foreign_percentage " +
            "FROM job_post " +
            "WHERE sector IS NOT NULL " +
            "GROUP BY sector " +
            "HAVING job_count >= 5 " +
            "ORDER BY job_count DESC"
        );
    }
}
