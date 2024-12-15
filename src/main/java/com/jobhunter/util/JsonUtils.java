package com.jobhunter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonArray readJsonArrayFromFile(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        return JsonParser.parseReader(reader).getAsJsonArray();
    }

    public static void saveJsonToFile(JsonArray jsonArray, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Process LLM response and append it to database.json
     * @param llmResponse The raw response string from the LLM
     * @param databasePath Path to the database.json file
     * @throws IOException If there's an error reading/writing the file
     */
    public static void processAndAppendLLMResponse(String llmResponse, String databasePath) throws IOException {
        // Create database file if it doesn't exist
        if (!Files.exists(Paths.get(databasePath))) {
            Files.createDirectories(Paths.get(databasePath).getParent());
            Files.writeString(Paths.get(databasePath), "[]");
        }

        // Read existing database
        JsonArray database;
        try (FileReader reader = new FileReader(databasePath)) {
            JsonElement element = JsonParser.parseReader(reader);
            database = element.isJsonArray() ? element.getAsJsonArray() : new JsonArray();
        } catch (IOException e) {
            database = new JsonArray();
        }

        // Process the LLM response
        // Remove surrounding quotes if present and unescape the JSON string
        String cleanResponse = llmResponse.trim();
        if (cleanResponse.startsWith("\"") && cleanResponse.endsWith("\"")) {
            cleanResponse = cleanResponse.substring(1, cleanResponse.length() - 1);
        }
        // Unescape the JSON string
        cleanResponse = cleanResponse.replace("\\\"", "\"")
                                   .replace("\\\\", "\\")
                                   .replace("\\/", "/");

        // Parse the cleaned response
        try {
            JsonElement jobOffer = JsonParser.parseString(cleanResponse);
            if (jobOffer.isJsonObject()) {
                // Add the job offer to the database array
                database.add(jobOffer);

                // Write the updated database back to file with pretty printing
                try (FileWriter writer = new FileWriter(databasePath)) {
                    gson.toJson(database, writer);
                }
            } else {
                throw new IllegalArgumentException("Processed LLM response is not a valid JSON object");
            }
        } catch (Exception e) {
            System.err.println("Error processing LLM response: " + e.getMessage());
            System.err.println("Raw response: " + llmResponse);
            System.err.println("Cleaned response: " + cleanResponse);
            throw e;
        }
    }
}
