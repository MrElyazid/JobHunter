package com.jobhunter.Cleaner;

import com.jobhunter.Cleaner.Cleaners.*;
import com.jobhunter.Cleaner.interfaces.JobCleaner;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class RegExCleaner {
    private static final String LAST_SCRAPPE_DIR = "lastScrappe";
    private static final String DATABASE_PATH = "database/output_database.json";

    private final Map<String, JobCleaner> cleaners;

    public RegExCleaner() {
        this.cleaners = new HashMap<>();
        this.cleaners.put("rekrute", new RekruteCleaner());
        this.cleaners.put("emploima", new EmploiMaCleaner());
        this.cleaners.put("anapec", new AnapecCleaner());
        this.cleaners.put("khdmama", new KhdmaMaCleaner());
        this.cleaners.put("marocannonces", new MarocAnnoncesCleaner());
    }

    public void processAllJobOffers() {
        try {
            File lastScrappeDir = new File(LAST_SCRAPPE_DIR);
            File[] jsonFiles = lastScrappeDir.listFiles((dir, name) -> name.endsWith(".json"));

            if (jsonFiles == null || jsonFiles.length == 0) {
                System.out.println("No JSON files found in lastScrappe directory");
                return;
            }

            JSONArray cleanedJobOffers = new JSONArray();

            for (File jsonFile : jsonFiles) {
                System.out.println("Processing file: " + jsonFile.getName());
                String content = new String(Files.readAllBytes(jsonFile.toPath()));
                JSONArray jobOffers = new JSONArray(content);

                for (int i = 0; i < jobOffers.length(); i++) {
                    JSONObject jobOffer = jobOffers.getJSONObject(i);
                    JSONObject cleanedJobOffer = cleanJobOffer(jobOffer, getSourceFromFileName(jsonFile.getName()));
                    if (cleanedJobOffer != null) {
                        cleanedJobOffers.put(cleanedJobOffer);
                    }
                }
            }

            // Write cleaned job offers to database
            Files.write(Paths.get(DATABASE_PATH), cleanedJobOffers.toString(2).getBytes());
            System.out.println("Cleaned job offers have been written to " + DATABASE_PATH);

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

    public static void main(String[] args) {
        RegExCleaner cleaner = new RegExCleaner();
        cleaner.processAllJobOffers();
    }
}
