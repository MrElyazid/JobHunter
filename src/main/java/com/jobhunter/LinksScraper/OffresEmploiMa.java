package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

import java.io.IOException;

public class OffresEmploiMa {

    private static final String BASE_URL = "https://www.offres-emploi.ma/emploi-maroc.mc?p=";
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray(); // JSON Array to hold all job data

        for (int page = 0; page < MAX_PAGES; page++) { // Iterate through pages (0 to 9)
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();

                // Select the main container for all job listings
                Elements jobElements = doc.select("div.job-list div.align-items-center div.mb-8 article.job-search-item");

                for (Element jobElement : jobElements) {
                    // Extract main job offer title and link
                    String mainJobTitle = jobElement.select("h2 > a").attr("title");
                    String mainJobLink = jobElement.select("h2 > a").attr("href");

                    // Create JSON object for the main job
                    JsonObject mainJobJson = new JsonObject();
                    mainJobJson.addProperty("title", mainJobTitle);
                    mainJobJson.addProperty("link", mainJobLink);

                    // Handle similar job offers inside the current job
                    JsonArray similarJobsArray = new JsonArray(); // JSON Array for similar jobs
                    Elements similarJobs = jobElement.select("div.collapse.similar-jobs ol > li > a"); // Find all similar job links

                    for (Element similarJob : similarJobs) {
                        String similarJobTitle = similarJob.text();
                        String similarJobLink = similarJob.attr("href");

                        // Create JSON object for the similar job
                        JsonObject similarJobJson = new JsonObject();
                        similarJobJson.addProperty("title", similarJobTitle);
                        similarJobJson.addProperty("link", similarJobLink);

                        // Add to similar jobs array
                        similarJobsArray.add(similarJobJson);
                    }

                    // Add similar jobs to the main job JSON object
                    mainJobJson.add("similarJobs", similarJobsArray);

                    // Add main job (with its similar jobs) to the job posts array
                    jobPostsArray.add(mainJobJson);
                }

            } catch (IOException e) {
                System.err.println("Error scraping page " + page + ": " + e.getMessage());
                e.printStackTrace();
            }
        }

        // Save the JSON array to a file using the JsonUtils method
        JsonUtils.saveJsonToFile(jobPostsArray, "data/OffresEmploiMaLinks.json");
        System.out.println("OffresEmploiMa scraping completed. Results saved to data/OffresEmploiMaLinks.json");
    }
}
