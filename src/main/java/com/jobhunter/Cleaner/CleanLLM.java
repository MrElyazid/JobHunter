package com.jobhunter.Cleaner;

import org.json.JSONArray;
import org.json.JSONObject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CleanLLM {
    private static final String DATABASE_PATH = "database/database.json";

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
