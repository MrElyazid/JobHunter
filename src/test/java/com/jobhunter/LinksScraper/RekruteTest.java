package com.jobhunter.LinksScraper;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import java.io.File;
import com.google.gson.JsonArray;
import com.jobhunter.util.JsonUtils;

public class RekruteTest {
    private Rekrute scraper;
    private static final String OUTPUT_FILE = "data/RekruteLinks.json";

    @Before
    public void setUp() {
        scraper = new Rekrute();
        // Limit pages for testing
        Rekrute.setMaxPages(1);
        // Ensure data directory exists
        new File("data").mkdirs();
    }

    @After
    public void tearDown() {
        // Clean up test output
        File outputFile = new File(OUTPUT_FILE);
        if (outputFile.exists()) {
            outputFile.delete();
        }
    }

    @Test
    public void testScrapeCreatesOutputFile() {
        scraper.scrape();
        File outputFile = new File(OUTPUT_FILE);
        assertTrue("Output file should be created", outputFile.exists());
    }

    @Test
    public void testScrapeOutputFormat() {
        scraper.scrape();
        try {
            JsonArray jobs = JsonUtils.readJsonArrayFromFile(OUTPUT_FILE);
            assertNotNull("Jobs array should not be null", jobs);
            
            if (jobs.size() > 0) {
                // Test first job structure
                assertTrue("Job should have title", jobs.get(0).getAsJsonObject().has("title"));
                assertTrue("Job should have link", jobs.get(0).getAsJsonObject().has("link"));
                
                // Test link format
                String link = jobs.get(0).getAsJsonObject().get("link").getAsString();
                assertTrue("Link should start with https://www.rekrute.com", 
                    link.startsWith("https://www.rekrute.com"));
            }
        } catch (Exception e) {
            fail("Failed to read output file: " + e.getMessage());
        }
    }

    @Test
    public void testMaxPagesLimit() {
        int testMaxPages = 2;
        Rekrute.setMaxPages(testMaxPages);
        try {
            java.lang.reflect.Field maxPagesField = Rekrute.class.getDeclaredField("MAX_PAGES");
            maxPagesField.setAccessible(true);
            int actualMaxPages = (int) maxPagesField.get(null);
            assertEquals("Max pages should be set correctly", testMaxPages, actualMaxPages);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail("Failed to access MAX_PAGES field: " + e.getMessage());
        }
    }
}
