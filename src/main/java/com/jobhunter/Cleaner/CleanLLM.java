package com.jobhunter.Cleaner;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class CleanLLM {
    private static final String DATABASE_PATH = "database/database.json";
    
    // Map of special characters to their normalized versions
    private static final Map<String, String> CHAR_REPLACEMENTS = new HashMap<>();
    static {
        // French accented characters
        CHAR_REPLACEMENTS.put("é", "e");
        CHAR_REPLACEMENTS.put("è", "e");
        CHAR_REPLACEMENTS.put("ê", "e");
        CHAR_REPLACEMENTS.put("ë", "e");
        CHAR_REPLACEMENTS.put("à", "a");
        CHAR_REPLACEMENTS.put("â", "a");
        CHAR_REPLACEMENTS.put("ä", "a");
        CHAR_REPLACEMENTS.put("î", "i");
        CHAR_REPLACEMENTS.put("ï", "i");
        CHAR_REPLACEMENTS.put("ô", "o");
        CHAR_REPLACEMENTS.put("ö", "o");
        CHAR_REPLACEMENTS.put("ù", "u");
        CHAR_REPLACEMENTS.put("û", "u");
        CHAR_REPLACEMENTS.put("ü", "u");
        CHAR_REPLACEMENTS.put("ç", "c");
        // Uppercase versions
        CHAR_REPLACEMENTS.put("É", "E");
        CHAR_REPLACEMENTS.put("È", "E");
        CHAR_REPLACEMENTS.put("Ê", "E");
        CHAR_REPLACEMENTS.put("Ë", "E");
        CHAR_REPLACEMENTS.put("À", "A");
        CHAR_REPLACEMENTS.put("Â", "A");
        CHAR_REPLACEMENTS.put("Ä", "A");
        CHAR_REPLACEMENTS.put("Î", "I");
        CHAR_REPLACEMENTS.put("Ï", "I");
        CHAR_REPLACEMENTS.put("Ô", "O");
        CHAR_REPLACEMENTS.put("Ö", "O");
        CHAR_REPLACEMENTS.put("Ù", "U");
        CHAR_REPLACEMENTS.put("Û", "U");
        CHAR_REPLACEMENTS.put("Ü", "U");
        CHAR_REPLACEMENTS.put("Ç", "C");
    }

    /**
     * Normalizes special characters in text to their basic ASCII equivalents
     * and removes single quotes.
     */
    private static String normalizeText(String text) {
        if (text == null) return null;
        
        // Replace special characters
        String normalized = text;
        for (Map.Entry<String, String> entry : CHAR_REPLACEMENTS.entrySet()) {
            normalized = normalized.replace(entry.getKey(), entry.getValue());
        }
        
        // Remove single quotes
        normalized = normalized.replace("'", "");
        
        return normalized;
    }

    /**
     * Recursively processes JSON values to normalize special characters
     */
    private static Object processJsonValue(Object value) {
        if (value instanceof String) {
            return normalizeText((String) value);
        } else if (value instanceof JSONObject) {
            JSONObject jsonObject = (JSONObject) value;
            JSONObject newObject = new JSONObject();
            for (String key : jsonObject.keySet()) {
                newObject.put(key, processJsonValue(jsonObject.get(key)));
            }
            return newObject;
        } else if (value instanceof JSONArray) {
            JSONArray jsonArray = (JSONArray) value;
            JSONArray newArray = new JSONArray();
            for (int i = 0; i < jsonArray.length(); i++) {
                newArray.put(processJsonValue(jsonArray.get(i)));
            }
            return newArray;
        }
        return value;
    }

    public static void main(String[] args) {
        try {
            // Read the raw database.json file
            String content = Files.readString(Paths.get(DATABASE_PATH));
            
            // Split content into lines (each line is a job offer)
            String[] lines = content.split("\n");
            
            // Create a new JSON array to hold cleaned entries
            JSONArray cleanedDatabase = new JSONArray();
            
            // Process each line
            for (String line : lines) {
                try {
                    // Skip empty lines
                    if (line.trim().isEmpty() || line.equals("{}")) {
                        continue;
                    }
                    
                    // Remove surrounding quotes if present
                    String cleaned = line.trim();
                    if (cleaned.startsWith("\"") && cleaned.endsWith("\"")) {
                        cleaned = cleaned.substring(1, cleaned.length() - 1);
                    }
                    
                    // Remove escaped quotes and other escape sequences
                    cleaned = cleaned.replace("\\\"", "\"")
                                   .replace("\\\\", "\\")
                                   .replace("\\/", "/");
                    
                    // Parse as JSON object
                    JSONObject jobOffer = new JSONObject(cleaned);
                    
                    // Process all string values in the JSON object to normalize special characters
                    jobOffer = (JSONObject) processJsonValue(jobOffer);
                    
                    // Ensure all fields are present and in the correct format
                    ensureFieldsPresent(jobOffer);
                    
                    // Add to cleaned database
                    cleanedDatabase.put(jobOffer);
                    
                } catch (Exception e) {
                    System.err.println("Error processing line: " + line);
                    System.err.println("Error: " + e.getMessage());
                }
            }
            
            // Write the cleaned database back to file with pretty printing
            Files.writeString(Paths.get(DATABASE_PATH), cleanedDatabase.toString(2));
            
            System.out.println("Successfully cleaned database.json");
            System.out.println("Processed " + cleanedDatabase.length() + " job offers");
            
        } catch (Exception e) {
            System.err.println("Error cleaning database: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Ensures all required fields are present in the job offer JSON object
     * and converts them to the correct data type if necessary.
     */
    private static void ensureFieldsPresent(JSONObject jobOffer) {
        // Ensure string fields
        ensureStringField(jobOffer, "location");
        ensureStringField(jobOffer, "sector");
        ensureStringField(jobOffer, "job_description");
        ensureStringField(jobOffer, "company");
        ensureStringField(jobOffer, "company_description");
        ensureStringField(jobOffer, "contract_type");
        ensureStringField(jobOffer, "source");
        ensureStringField(jobOffer, "link");
        ensureStringField(jobOffer, "title");
        ensureStringField(jobOffer, "company_address");
        ensureStringField(jobOffer, "company_website");
        ensureStringField(jobOffer, "region");
        ensureStringField(jobOffer, "desired_profile");
        ensureStringField(jobOffer, "personality_traits");
        ensureStringField(jobOffer, "languages");
        ensureStringField(jobOffer, "language_proficiency");
        ensureStringField(jobOffer, "recommended_skills");
        ensureStringField(jobOffer, "job");

        // Ensure numeric fields
        ensureNumericField(jobOffer, "min_salary");
        ensureNumericField(jobOffer, "min_experience");

        // Ensure boolean fields
        ensureBooleanField(jobOffer, "is_remote");
        ensureBooleanField(jobOffer, "foreign_company");
        ensureBooleanField(jobOffer, "is_internship");

        // Ensure date fields
        ensureDateField(jobOffer, "application_date");
        ensureDateField(jobOffer, "date_of_publication");

        // Ensure array fields
        ensureArrayField(jobOffer, "hard_skills");
        ensureArrayField(jobOffer, "soft_skills");
        ensureArrayField(jobOffer, "diploma");
    }

    private static void ensureStringField(JSONObject jobOffer, String fieldName) {
        if (!jobOffer.has(fieldName) || jobOffer.isNull(fieldName)) {
            jobOffer.put(fieldName, "");
        } else {
            jobOffer.put(fieldName, jobOffer.getString(fieldName));
        }
    }

    private static void ensureNumericField(JSONObject jobOffer, String fieldName) {
        if (!jobOffer.has(fieldName) || jobOffer.isNull(fieldName)) {
            jobOffer.put(fieldName, 0);
        } else {
            try {
                jobOffer.put(fieldName, jobOffer.getNumber(fieldName));
            } catch (Exception e) {
                jobOffer.put(fieldName, 0);
            }
        }
    }

    private static void ensureBooleanField(JSONObject jobOffer, String fieldName) {
        if (!jobOffer.has(fieldName) || jobOffer.isNull(fieldName)) {
            jobOffer.put(fieldName, false);
        } else {
            jobOffer.put(fieldName, jobOffer.getBoolean(fieldName));
        }
    }

    private static void ensureDateField(JSONObject jobOffer, String fieldName) {
        if (!jobOffer.has(fieldName) || jobOffer.isNull(fieldName)) {
            jobOffer.put(fieldName, "");
        } else {
            // Assuming the date is already in the correct format (YYYY-MM-DD)
            jobOffer.put(fieldName, jobOffer.getString(fieldName));
        }
    }

    private static void ensureArrayField(JSONObject jobOffer, String fieldName) {
        if (!jobOffer.has(fieldName) || jobOffer.isNull(fieldName)) {
            jobOffer.put(fieldName, new JSONArray());
        } else {
            // Ensure it's a JSONArray
            try {
                jobOffer.getJSONArray(fieldName);
            } catch (Exception e) {
                jobOffer.put(fieldName, new JSONArray());
            }
        }
    }
}
