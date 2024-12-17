package com.jobhunter.pages.refreshDb.services;

import com.jobhunter.pages.refreshDb.models.ScrapingSite;
import com.jobhunter.pages.refreshDb.interfaces.Scraper;
import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import com.jobhunter.pages.refreshDb.factories.ScraperFactory;
import com.jobhunter.pages.refreshDb.factories.ProcessorFactory;
import com.jobhunter.pages.refreshDb.factories.ProcessorFactory.ProcessorType;

import java.util.List;
import java.util.function.Consumer;

public class ScrapingService {
    private final Consumer<String> logCallback;
    private final Consumer<Integer> progressCallback;

    public ScrapingService(Consumer<String> logCallback, Consumer<Integer> progressCallback) {
        this.logCallback = logCallback;
        this.progressCallback = progressCallback;
    }

    public void startScraping(List<ScrapingSite> selectedSites) throws Exception {
        try {
            // Step 1: Links Scraping
            logCallback.accept("Starting links scraping process...");
            progressCallback.accept(10);
            
            for (ScrapingSite site : selectedSites) {
                if (site.isSelected()) {
                    logCallback.accept("Scraping links from " + site.getName() + "...");
                    Scraper linksScraper = ScraperFactory.createLinksScraper(site.getName());
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
            
            // Step 3: Data Cleaning
            logCallback.accept("\nCleaning scraped data...");
            progressCallback.accept(70);
            DataProcessor cleaner = ProcessorFactory.createProcessor(ProcessorType.CLEANER);
            cleaner.execute();
            
            // Step 4: Database Update
            logCallback.accept("\nUpdating database...");
            progressCallback.accept(90);
            DataProcessor dbInserter = ProcessorFactory.createProcessor(ProcessorType.DATABASE_INSERTER);
            dbInserter.execute();
            
            progressCallback.accept(100);
            logCallback.accept("\nDatabase refresh completed successfully!");
            
        } catch (Exception e) {
            logCallback.accept("\nError during refresh: " + e.getMessage());
            throw e;
        }
    }
}
