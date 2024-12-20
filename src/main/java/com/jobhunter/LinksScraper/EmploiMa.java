package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

import java.io.IOException;

public class EmploiMa {

    private static final String BASE_URL = "https://www.emploi.ma/recherche-jobs-maroc?page=";
    private static int MAX_PAGES = 12; // Changed from final to allow modification

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();

                Elements jobCards = doc.select(".page-search-jobs-content .card-job");
                
                // If no jobs found on this page, break the loop
                if (jobCards.isEmpty()) {
                    System.out.println("No more jobs found on page " + page + ", stopping pagination");
                    break;
                }

                for (Element card : jobCards) {
                    String jobLink = card.attr("data-href");
                    String jobTitle = card.select("div.card-job-detail h3 a").text();
                    
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);
                    
                    jobPostsArray.add(jobJson);
                }

                System.out.println("Processed page " + page + " - Found " + jobCards.size() + " jobs");

            } catch (IOException e) {
                System.err.println("Error processing page " + page + ": " + e.getMessage());
                break; // Stop on error to avoid unnecessary requests
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/EmploiMaLinks.json");
        System.out.println("EmploiMa scraping completed. Results saved to data/EmploiMaLinks.json");
    }

    // Method to set the maximum number of pages to scrape
    public static void setMaxPages(int pages) {
        MAX_PAGES = Math.max(1, Math.min(pages, 50)); // Ensure between 1 and 50 pages
    }

    public static void main(String[] args) {
        EmploiMa emploiMa = new EmploiMa();
        emploiMa.scrape();
    }
}
