package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

import java.io.IOException;

public class MJobMa {

    private static final String BASE_URL = "http://m-job.ma/recherche?page=";
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 0; page < MAX_PAGES; page++) { // Pages from 0 to 9
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.offers-boxes > div.offer-box"); // Select all job offer elements

                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.select("div.offer-heading > h3.offer-title > a").attr("href"); // Extract href
                    String jobTitle = jobPost.select("div.offer-heading > h3.offer-title > a").text(); // Extract text of the title

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
        JsonUtils.saveJsonToFile(jobPostsArray, "data/MJobLinks.json");
        System.out.println("MJob scraping completed. Results saved to data/MJobLinks.json");
    }
}
