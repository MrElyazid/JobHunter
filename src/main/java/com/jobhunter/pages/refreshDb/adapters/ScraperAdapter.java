package com.jobhunter.pages.refreshDb.adapters;

import com.jobhunter.pages.refreshDb.interfaces.Scraper;

public class ScraperAdapter implements Scraper {
    private final Class<?> scraperClass;

    public ScraperAdapter(Class<?> scraperClass) {
        this.scraperClass = scraperClass;
    }

    @Override
    public void execute() throws Exception {
        // Call the main method of the scraper class using reflection
        scraperClass.getMethod("main", String[].class)
                   .invoke(null, (Object) new String[0]);
    }
}
