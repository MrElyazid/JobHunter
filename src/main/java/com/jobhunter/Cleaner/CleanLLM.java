package com.jobhunter.Cleaner;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
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
}
