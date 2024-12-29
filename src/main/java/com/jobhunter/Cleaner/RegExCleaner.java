package com.jobhunter.Cleaner;

import java.util.List;
import java.util.Set;
import java.util.HashSet;
import com.jobhunter.Cleaner.Cleaners.*;
import com.jobhunter.Cleaner.interfaces.JobCleaner;
import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import com.jobhunter.database.InsertJson;
import com.jobhunter.database.JsonStructureConverter;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RegExCleaner implements DataProcessor {
    private static final String LAST_SCRAPPE_DIR = "lastScrappe";
    private static final String DATABASE_PATH = "database/output_database.json";
    private final Map<String, JobCleaner> cleaners;
    private final List<String> selectedSites;
    private String databasePath = DATABASE_PATH;
    private boolean appendMode = false;

    public RegExCleaner(List<String> selectedSites) {
        this.selectedSites = selectedSites;
        this.cleaners = new HashMap<>();
        // Initialize cleaners with lowercase keys
        this.cleaners.put("rekrute", new RekruteCleaner());
        this.cleaners.put("emploima", new EmploiMaCleaner());
        this.cleaners.put("anapec", new AnapecCleaner());
        this.cleaners.put("khdmama", new KhdmaMaCleaner());
        this.cleaners.put("marocannonces", new MarocAnnoncesCleaner());
        
        // Print selected sites for debugging
        System.out.println("Selected sites for cleaning: " + selectedSites);
    }

    public void processAllJobOffers() {
        // Create a set of expected file names based on selected sites
        Set<String> expectedFiles = new HashSet<>();
        for (String site : selectedSites) {
            // Handle both possible filename formats
            String siteLower = site.toLowerCase();
            expectedFiles.add(siteLower + "Data.json"); // Original format
            expectedFiles.add(siteLower + "data.json"); // Lowercase variant
            System.out.println("Adding expected file patterns: " + siteLower + "Data.json, " + siteLower + "data.json");
        }
        try {
            File lastScrappeDir = new File(LAST_SCRAPPE_DIR);
            File[] jsonFiles = lastScrappeDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (jsonFiles == null || jsonFiles.length == 0) {
                System.out.println("No JSON files found in lastScrappe directory");
                return;
            }

            JSONArray cleanedJobOffers = new JSONArray();

            for (File jsonFile : jsonFiles) {
                // Only process files for selected sites
                String fileName = jsonFile.getName();
                String fileNameLower = fileName.toLowerCase();
                
                // Debug print
                System.out.println("Checking file: " + fileName + " (lowercase: " + fileNameLower + ")");
                System.out.println("Expected files set contains: " + expectedFiles);
                
                if (!expectedFiles.contains(fileNameLower)) {
                    System.out.println("Skipping file: " + jsonFile.getName() + " (not in selected sites)");
                    continue;
                }
                System.out.println("Processing file: " + jsonFile.getName());
                String content = new String(Files.readAllBytes(jsonFile.toPath()));
                JSONArray jobOffers = new JSONArray(content);

                for (int i = 0; i < jobOffers.length(); i++) {
                    JSONObject jobOffer = jobOffers.getJSONObject(i);
                    String source = getSourceFromFileName(jsonFile.getName());
                    JSONObject cleanedJobOffer = cleanJobOffer(jobOffer, source);
                    if (cleanedJobOffer != null) {
                        cleanedJobOffers.put(cleanedJobOffer);
                        System.out.println("Successfully cleaned job offer from " + source);
                    } else {
                        System.out.println("Skipped job offer from " + source + " (failed cleaning)");
                    }
                }
            }

            // Ensure database directory exists
            new File("database").mkdirs();

            // Write cleaned data to output file
            Path outputFile = Paths.get(databasePath);
            String jsonData = cleanedJobOffers.toString(2); // Use pretty printing
            
            // Verify JSON is valid before writing
            try {
                new JSONArray(jsonData); // Test parsing
                
                // Write to file
                Files.write(outputFile, jsonData.getBytes());
                System.out.println("Cleaned job offers written to " + databasePath);
            } catch (Exception e) {
                System.err.println("Error validating JSON data: " + e.getMessage());
                System.err.println("JSON data: " + jsonData);
                throw new RuntimeException("Invalid JSON data generated", e);
            }

        } catch (Exception e) {
            System.err.println("Error processing job offers: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JSONObject cleanJobOffer(JSONObject rawJobOffer, String source) {
        JobCleaner cleaner = cleaners.get(source.toLowerCase());
        if (cleaner != null) {
            try {
                return cleaner.cleanJobOffer(rawJobOffer);
            } catch (Exception e) {
                System.err.println("Error cleaning job offer from " + source + ": " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            System.out.println("Unknown source: " + source + ". Skipping this job offer.");
        }
        return null;
    }

    private String getSourceFromFileName(String fileName) {
        fileName = fileName.toLowerCase();
        if (fileName.contains("rekrute")) return "rekrute";
        if (fileName.contains("emploima")) return "emploima";
        if (fileName.contains("anapec")) return "anapec";
        if (fileName.contains("khdmama")) return "khdmama";
        if (fileName.contains("marocannonces")) return "marocannonces";
        return "unknown";
    }

    @Override
    public void execute() {
        processAllJobOffers();
    }

    @Override
    public void setDatabasePath(String path) {
        this.databasePath = path;
    }

    @Override
    public void setAppendMode(boolean appendMode) {
        this.appendMode = appendMode;
    }

    public static void main(String[] args) {
        // For testing purposes, create a sample list of sites
        List<String> sampleSites = List.of("rekrute");
        RegExCleaner cleaner = new RegExCleaner(sampleSites);
        cleaner.processAllJobOffers();
    }
}
