package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import com.jobhunter.util.JsonUtils;

public class MarocAnnonces {

    private static final String BASE_URL = "https://www.marocannonces.com/categorie/309/Emploi/Offres-emploi/";
    private static final int START_PAGE = 2; // First page starts at 2
    private static int MAX_PAGES = 10; // Changed from END_PAGE and made non-final

    public void scrape() {
        JsonArray jobsArray = new JsonArray();

        try {
            for (int page = START_PAGE; page <= MAX_PAGES; page++) {
                String url = BASE_URL + page + ".html";
                Document doc = Jsoup.connect(url).get();
                
                Elements jobListings = doc.select("div.content_box ul.cars-list li");
                
                // If no jobs found on this page, break the loop
                if (jobListings.isEmpty()) {
                    System.out.println("No more jobs found on page " + page + ", stopping pagination");
                    break;
                }
                
                for (Element job : jobListings) {
                    String title = job.select("a").attr("title");
                    String link = job.select("a").attr("href");

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", title);
                    jobJson.addProperty("link", "https://www.marocannonces.com/" + link);
                    
                    jobsArray.add(jobJson);
                }

                System.out.println("Processed page " + page + " - Found " + jobListings.size() + " jobs");
            }

            JsonUtils.saveJsonToFile(jobsArray, "data/MarocAnnoncesLinks.json");
            System.out.println("MarocAnnonces scraping completed. Results saved to data/MarocAnnoncesLinks.json");

        } catch (IOException e) {
            System.err.println("Error processing page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to set the maximum number of pages to scrape
    public static void setMaxPages(int pages) {
        MAX_PAGES = Math.max(START_PAGE, Math.min(pages + START_PAGE - 1, 50)); // Ensure between START_PAGE and 50 pages
    }

    public static void main(String[] args) {
        MarocAnnonces scraper = new MarocAnnonces();
        scraper.scrape();
    }
}
