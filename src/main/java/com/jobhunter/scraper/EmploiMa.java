package com.jobhunter.scraper;

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
    private static final int MAX_PAGES = 10;  // Adjust the number of pages to scrape

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();

                // Select the job cards from the page
                Elements jobCards = doc.select(".page-search-jobs-content .card-job");

                for (Element card : jobCards) {
                    // Extract job link, title, company name, and description
                    String jobLink = card.attr("data-href");
                    String jobTitle = card.select("div.card-job-detail h3 a").text();
                    String companyName = card.select("div.card-job-detail a.card-job-company").text();
                    String jobDescription = card.select("div.card-job-detail div.card-job-description p").text();

                    // Create a JSON object for the job
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);
                    jobJson.addProperty("company", companyName);
                    jobJson.addProperty("description", jobDescription);

                    // Add job JSON object to the array
                    jobPostsArray.add(jobJson);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Save the JSON array to a file using the JsonUtils method
        JsonUtils.saveJsonToFile(jobPostsArray, "data/EmploiMaLinks.json");
        System.out.println("EmploiMa scraping completed. Results saved to data/EmploiMaLinks.json");
    }
}
