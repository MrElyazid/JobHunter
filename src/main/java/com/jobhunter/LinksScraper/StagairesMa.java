package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

public class StagairesMa {

    private static final String BASE_URL = "https://www.stagiaires.ma/offres-de-stages-et-premier-emploi-maroc/?pages=";
    private static int MAX_PAGES = 6; // Changed from final to allow modification

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();
        
        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select(".ast-col-md-6");
                
                // If no jobs found on this page, break the loop
                if (jobPosts.isEmpty()) {
                    System.out.println("No more jobs found on page " + page + ", stopping pagination");
                    break;
                }
                
                for (Element jobPost : jobPosts) {
                    String jobTitle = jobPost.select("h3.tooltip_ethiques").text();
                    String jobLink = jobPost.select("a").attr("href");
                    
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);
                    
                    jobPostsArray.add(jobJson);
                }

                System.out.println("Processed page " + page + " - Found " + jobPosts.size() + " jobs");

            } catch (Exception e) {
                System.err.println("Error processing page " + page + ": " + e.getMessage());
                e.printStackTrace();
                break; // Stop on error to avoid unnecessary requests
            }
        }
        
        JsonUtils.saveJsonToFile(jobPostsArray, "data/StagairesMaLinks.json");
        System.out.println("StagairesMa scraping completed. Results saved to data/StagairesMaLinks.json");
    }

    // Method to set the maximum number of pages to scrape
    public static void setMaxPages(int pages) {
        MAX_PAGES = Math.max(1, Math.min(pages, 50)); // Ensure between 1 and 50 pages
    }

    public static void main(String[] args) {
        StagairesMa scraper = new StagairesMa();
        scraper.scrape();
    }
}