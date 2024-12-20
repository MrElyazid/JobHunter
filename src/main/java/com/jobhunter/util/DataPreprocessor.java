package com.jobhunter.util;

import java.io.*;
import java.util.*;
import java.nio.file.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DataPreprocessor {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final String[] HEADERS = {
        "company_website", "is_remote", "link", "source", "title", "company_address",
        "language_proficiency", "personality_traits", "soft_skills", "min_salary",
        "is_internship", "company", "diploma", "sector", "desired_profile", "languages",
        "recommended_skills", "foreign_company", "language_profeciency", "contract_type",
        "min_experience", "company_description", "job_description", "date_of_publication",
        "application_date", "location", "hard_skills", "region", "job"
    };

    public static Path preprocessJobData(String inputPath) throws IOException {
        Path tempPath = Files.createTempFile("preprocessed_job_data", ".csv");
        
        try (BufferedReader reader = new BufferedReader(new FileReader(inputPath));
             BufferedWriter writer = new BufferedWriter(new FileWriter(tempPath.toFile()))) {
            
            // Skip original header
            reader.readLine();
            
            // Write our standardized header
            writer.write(String.join(",", HEADERS));
            writer.newLine();
            
            String line;
            int lineNumber = 1;
            while ((line = reader.readLine()) != null) {
                lineNumber++;
                
                // Skip empty lines
                if (line.trim().isEmpty()) continue;
                
                try {
                    // Parse and clean the line
                    List<String> values = parseCSVLine(line);
                    
                    // Ensure we have exactly the right number of columns
                    while (values.size() < HEADERS.length) {
                        values.add("?"); // Add missing columns
                    }
                    if (values.size() > HEADERS.length) {
                        values = values.subList(0, HEADERS.length); // Truncate extra columns
                    }
                    
                    // Clean values
                    values = cleanValues(values);
                    
                    // Write cleaned line
                    writer.write(String.join(",", values));
                    writer.newLine();
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed line " + lineNumber + ": " + e.getMessage());
                }
            }
        }
        
        return tempPath;
    }
    
    private static List<String> parseCSVLine(String line) {
        List<String> values = new ArrayList<>();
        StringBuilder currentValue = new StringBuilder();
        boolean inQuotes = false;
        
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            
            if (c == '"') {
                inQuotes = !inQuotes;
                continue;
            }
            
            if (c == ',' && !inQuotes) {
                values.add(currentValue.toString().trim());
                currentValue = new StringBuilder();
            } else {
                currentValue.append(c);
            }
        }
        
        // Add the last value
        values.add(currentValue.toString().trim());
        
        return values;
    }
    
    private static List<String> cleanValues(List<String> values) {
        List<String> cleaned = new ArrayList<>();
        
        for (int i = 0; i < values.size(); i++) {
            String value = values.get(i).trim();
            
            // Remove surrounding quotes
            if (value.startsWith("\"") && value.endsWith("\"")) {
                value = value.substring(1, value.length() - 1);
            }
            
            // Clean based on column type
            switch (HEADERS[i]) {
                case "min_salary":
                    value = cleanSalary(value);
                    break;
                case "is_remote":
                case "is_internship":
                case "foreign_company":
                    value = cleanBoolean(value);
                    break;
                case "hard_skills":
                case "soft_skills":
                    value = cleanSkillsArray(value);
                    break;
                case "contract_type":
                    value = cleanContractType(value);
                    break;
                case "min_experience":
                    value = cleanExperience(value);
                    break;
                case "diploma":
                    value = cleanDiplomaArray(value);
                    break;
                case "date_of_publication":
                case "application_date":
                    value = cleanDate(value);
                    break;
                case "languages":
                case "language_proficiency":
                case "language_profeciency":
                    value = cleanLanguages(value);
                    break;
                default:
                    value = cleanGeneral(value);
            }
            
            // Escape any remaining commas
            value = value.replace(",", ";");
            
            // Quote the value if it contains special characters
            if (value.contains(";") || value.contains("\"") || value.contains("\n")) {
                value = "\"" + value.replace("\"", "\"\"") + "\"";
            }
            
            cleaned.add(value);
        }
        
        return cleaned;
    }
    
    private static String cleanSalary(String value) {
        try {
            double salary = Double.parseDouble(value);
            return salary > 0 ? value : "3000"; // Default to 3000 if invalid/zero
        } catch (NumberFormatException e) {
            return "3000"; // Default value
        }
    }
    
    private static String cleanBoolean(String value) {
        value = value.toLowerCase().trim();
        return value.equals("true") || value.equals("1") ? "1" : "0";
    }
    
    private static String cleanSkillsArray(String value) {
        if (value.equals("[]") || value.equals("N/A") || value.isEmpty()) {
            return "[]";
        }
        try {
            // Parse JSON array and clean individual skills
            List<String> skills = mapper.readValue(value, List.class);
            skills = skills.stream()
                          .map(s -> s.toString().trim().toLowerCase())
                          .filter(s -> !s.isEmpty())
                          .distinct()
                          .collect(Collectors.toList());
            return mapper.writeValueAsString(skills);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private static String cleanContractType(String value) {
        value = value.toUpperCase().trim();
        switch (value) {
            case "CDI":
            case "CDD":
            case "CI":
            case "STAGE":
                return value;
            default:
                return "CDI"; // Default to CDI
        }
    }
    
    private static String cleanExperience(String value) {
        try {
            int exp = Integer.parseInt(value);
            return String.valueOf(Math.max(0, Math.min(exp, 30))); // Clamp between 0-30 years
        } catch (NumberFormatException e) {
            return "0"; // Default to 0 years
        }
    }
    
    private static String cleanDiplomaArray(String value) {
        if (value.equals("[]") || value.equals("N/A") || value.isEmpty()) {
            return "[]";
        }
        try {
            List<String> diplomas = mapper.readValue(value, List.class);
            diplomas = diplomas.stream()
                              .map(d -> d.toString().trim().toUpperCase())
                              .filter(d -> !d.isEmpty())
                              .distinct()
                              .collect(Collectors.toList());
            return mapper.writeValueAsString(diplomas);
        } catch (Exception e) {
            return "[]";
        }
    }
    
    private static String cleanDate(String value) {
        if (value.equals("N/A") || value.isEmpty()) {
            return "2024-01-01"; // Default date
        }
        // Assuming dates are in YYYY-MM-DD format
        return value;
    }
    
    private static String cleanLanguages(String value) {
        if (value == null || value.isEmpty() || value.equals("N/A")) {
            return "?";
        }
        // Clean up language list
        String cleaned = value.replaceAll("\\s+", " ") // Normalize whitespace
                            .trim();
        return cleaned.isEmpty() ? "?" : cleaned;
    }
    
    private static String cleanGeneral(String value) {
        if (value.equals("N/A") || value.isEmpty()) {
            return "?"; // Weka's missing value symbol
        }
        // Clean and escape special characters
        return value.replace("\"", "'")
                   .replaceAll("\\s+", " ")  // Normalize whitespace
                   .trim();
    }
}
