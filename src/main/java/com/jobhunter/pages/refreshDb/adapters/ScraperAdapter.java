package com.jobhunter.pages.refreshDb.adapters;

import com.jobhunter.pages.refreshDb.interfaces.Scraper;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class ScraperAdapter implements Scraper {
    private final Object scraper;
    private final Class<?> scraperClass;

    public ScraperAdapter(Class<?> scraperClass) {
        try {
            this.scraperClass = scraperClass;
            this.scraper = scraperClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create scraper instance: " + e.getMessage());
        }
    }

    @Override
    public void execute() throws Exception {
        try {
            // First try to find and invoke a 'scrape' method
            try {
                Method scrapeMethod = scraperClass.getMethod("scrape");
                scrapeMethod.invoke(scraper);
                return;
            } catch (NoSuchMethodException e) {
                // If 'scrape' method doesn't exist, try 'main' method
                Method mainMethod = scraperClass.getMethod("main", String[].class);
                mainMethod.invoke(null, (Object) new String[0]);
            }
        } catch (Exception e) {
            throw new Exception("Failed to execute scraper: " + e.getMessage());
        }
    }

    @Override
    public void setPageCount(int pageCount) {
        try {
            // Try to set MAX_PAGES field if it exists
            try {
                Field maxPagesField = scraperClass.getDeclaredField("MAX_PAGES");
                maxPagesField.setAccessible(true);
                maxPagesField.set(scraper, pageCount);
            } catch (NoSuchFieldException e) {
                // If MAX_PAGES doesn't exist, try to set pageCount field
                Field pageCountField = scraperClass.getDeclaredField("pageCount");
                pageCountField.setAccessible(true);
                pageCountField.set(scraper, pageCount);
            }
        } catch (Exception e) {
            // Log error but don't throw exception as this is optional
            System.err.println("Warning: Could not set page count: " + e.getMessage());
        }
    }
}
