package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

import java.io.IOException;

public class KhdmaMa {

    private static final String BASE_URL = "https://khdma.ma/offres-emploi-maroc/";
    private static int MAX_PAGES = 10; // Changed from final to allow modification

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.listings-container > a.listing");

                // If no jobs found on this page, break the loop
                if (jobPosts.isEmpty()) {
                    System.out.println("No more jobs found on page " + page + ", stopping pagination");
                    break;
                }

                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.attr("href");
                    String jobTitle = jobPost.select("div.listing-title > h4").text();

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);

                    jobPostsArray.add(jobJson);
                }

                System.out.println("Processed page " + page + " - Found " + jobPosts.size() + " jobs");

            } catch (IOException e) {
                System.err.println("Error processing page " + page + ": " + e.getMessage());
                break; // Stop on error to avoid unnecessary requests
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/KhdmaLinks.json");
        System.out.println("Khdma scraping completed. Results saved to data/KhdmaLinks.json");
    }

    // Method to set the maximum number of pages to scrape
    public static void setMaxPages(int pages) {
        MAX_PAGES = Math.max(1, Math.min(pages, 50)); // Ensure between 1 and 50 pages
    }

    public static void main(String[] args) {
        KhdmaMa scraper = new KhdmaMa();
        scraper.scrape();
    }
}
