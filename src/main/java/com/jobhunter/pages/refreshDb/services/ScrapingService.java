package com.jobhunter.pages.refreshDb.services;

import com.jobhunter.pages.refreshDb.models.ScrapingSite;
import com.jobhunter.pages.refreshDb.interfaces.Scraper;
import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import com.jobhunter.pages.refreshDb.factories.ScraperFactory;
import com.jobhunter.pages.refreshDb.factories.ProcessorFactory;
import com.jobhunter.pages.refreshDb.factories.ProcessorFactory.ProcessorType;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;
import java.util.function.Consumer;

public class ScrapingService {
    private final Consumer<String> logCallback;
    private final Consumer<Integer> progressCallback;
    private static final String LLM_DATABASE_PATH = "database/database.json";
    private static final String REGEX_DATABASE_PATH = "database/database1.json";
    private static final String OUTPUT_DATABASE_PATH = "database/output_database.json";

    public ScrapingService(Consumer<String> logCallback, Consumer<Integer> progressCallback) {
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
    }

    public void startScraping(List<ScrapingSite> selectedSites, String cleaningPipeline, boolean appendMode) throws Exception {
        try {
            // Handle database mode
            String databasePath = cleaningPipeline.equals("regex") ? REGEX_DATABASE_PATH : LLM_DATABASE_PATH;
            if (!appendMode) {
                logCallback.accept("Creating new database...");
                File dbFile = new File(databasePath);
                if (dbFile.exists()) {
                    Files.delete(dbFile.toPath());
                }
            }

            // Step 1: Links Scraping
            logCallback.accept("Starting links scraping process...");
            progressCallback.accept(10);
            
            for (ScrapingSite site : selectedSites) {
                if (site.isSelected()) {
                    logCallback.accept("Scraping links from " + site.getName() + " (" + site.getPageCount() + " pages)...");
                    Scraper linksScraper = ScraperFactory.createLinksScraper(site.getName());
                    linksScraper.setPageCount(site.getPageCount()); // Set page count before execution
                    linksScraper.execute();
                }
            }
            
            // Step 2: Data Scraping
            logCallback.accept("\nStarting data scraping process...");
            progressCallback.accept(40);
            
            for (ScrapingSite site : selectedSites) {
                if (site.isSelected()) {
                    logCallback.accept("Scraping data from " + site.getName() + "...");
                    Scraper dataScraper = ScraperFactory.createDataScraper(site.getName());
                    dataScraper.execute();
                }
            }
            
            // Step 3: Data Cleaning based on selected pipeline
            logCallback.accept("\nCleaning scraped data...");
            progressCallback.accept(70);
            
            if (cleaningPipeline.equals("regex")) {
                logCallback.accept("Using RegEx cleaning pipeline...");
                // Get list of selected site names
                List<String> selectedSiteNames = selectedSites.stream()
                    .filter(ScrapingSite::isSelected)
                    .map(site -> site.getName().toLowerCase()) // Convert to lowercase for RegExCleaner
                    .collect(Collectors.toList());
                
                // Step 3a: RegEx Cleaning
                DataProcessor regexCleaner = ProcessorFactory.createProcessor(ProcessorType.REGEX_CLEANER, selectedSiteNames);
                regexCleaner.execute();
                
                // Step 3b: Run JsonStructureConverter
                logCallback.accept("Converting JSON structure...");
                com.jobhunter.database.JsonStructureConverter.main(new String[0]);
                logCallback.accept("JSON structure conversion completed");
                
                // Step 4: Database Update
                logCallback.accept("\nUpdating database...");
                progressCallback.accept(90);
                
                // Set the appropriate database file path in the processor
                DataProcessor dbInserter = ProcessorFactory.createProcessor(ProcessorType.DATABASE_INSERTER);
                dbInserter.setDatabasePath(REGEX_DATABASE_PATH);
                dbInserter.setAppendMode(appendMode);
                dbInserter.execute();
                
                // Clean up only after successful database insertion
                new File(OUTPUT_DATABASE_PATH).delete();
                
            } else {
                logCallback.accept("Using LLM cleaning pipeline...");
                DataProcessor llmCleaner = ProcessorFactory.createProcessor(ProcessorType.LLM_CLEANER);
                llmCleaner.execute();
                
                // Step 4: Database Update
                logCallback.accept("\nUpdating database...");
                progressCallback.accept(90);
                
                // Set the appropriate database file path in the processor
                DataProcessor dbInserter = ProcessorFactory.createProcessor(ProcessorType.DATABASE_INSERTER);
                dbInserter.setDatabasePath(LLM_DATABASE_PATH);
                dbInserter.setAppendMode(appendMode);
                dbInserter.execute();
            }
            
            progressCallback.accept(100);
            logCallback.accept("\nDatabase refresh completed successfully!");
            
        } catch (Exception e) {
            logCallback.accept("\nError during refresh: " + e.getMessage());
            throw e;
        }
    }
}
