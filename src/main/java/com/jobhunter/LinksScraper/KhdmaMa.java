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
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 1; page <= MAX_PAGES; page++) { // Iterate from /1 to /10
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.listings-container > a.listing"); // Select all job offer elements

                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.attr("href"); // Extract href attribute for the link
                    String jobTitle = jobPost.select("div.listing-title > h4").text(); // Extract job title

                    // Create a JSON object for the job
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);

                    // Add job JSON object to the array
                    jobPostsArray.add(jobJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Save the JSON array to a file using the JsonUtils method
        JsonUtils.saveJsonToFile(jobPostsArray, "data/KhdmaLinks.json");
        System.out.println("Khdma scraping completed. Results saved to data/KhdmaLinks.json");
    }
}
