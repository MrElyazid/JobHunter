package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

import com.jobhunter.util.JsonUtils;

public class Rekrute {

    private static final String BASE_URL = "https://www.rekrute.com/offres.html?s=1&p=";
    private static int MAX_PAGES = 10; // Changed from final to allow modification

    public void scrape() {
        JsonArray jobsArray = new JsonArray();

        try {
            for (int page = 1; page <= MAX_PAGES; page++) {
                String url = BASE_URL + page + "&o=1";
                Document doc = Jsoup.connect(url).get();
                
                Elements jobListings = doc.select("div.col-sm-10.col-xs-12");
                
                // If no jobs found on this page, break the loop
                if (jobListings.isEmpty()) {
                    System.out.println("No more jobs found on page " + page + ", stopping pagination");
                    break;
                }
                
                for (Element job : jobListings) {
                    String title = job.select("a.titreJob").text().replace("\"", "'");
                    String link = "https://www.rekrute.com" + job.select("a.titreJob").attr("href");

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", title);
                    jobJson.addProperty("link", link);

                    jobsArray.add(jobJson);
                }

                System.out.println("Processed page " + page + " - Found " + jobListings.size() + " jobs");
            }

            JsonUtils.saveJsonToFile(jobsArray, "data/RekruteLinks.json");
            System.out.println("Rekrute scraping completed. Results saved to data/RekruteLinks.json");

        } catch (IOException e) {
            System.err.println("Error processing page: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to set the maximum number of pages to scrape
    public static void setMaxPages(int pages) {
        MAX_PAGES = Math.max(1, Math.min(pages, 50)); // Ensure between 1 and 50 pages
    }

    public static void main(String[] args) {
        Rekrute scraper = new Rekrute();
        scraper.scrape();
    }
}
