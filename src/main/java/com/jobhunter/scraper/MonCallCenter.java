package com.jobhunter.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;
import java.io.IOException;

public class MonCallCenter {

    private static final String BASE_URL = "https://www.moncallcenter.ma/offres-emploi/";
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();
        
        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page + "/";
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.offres");

                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.select("div:nth-child(1) > div:nth-child(2) > h2:nth-child(1) > a:nth-child(1)").attr("href");
                    String jobTitle = jobPost.select("div:nth-child(1) > div:nth-child(2) > h2:nth-child(1) > a:nth-child(1)").text();
                    String jobDescription = jobPost.select("div:nth-child(1) > div:nth-child(2) > div:nth-child(3) > p:nth-child(1)").text();
                    String jobLocationAndDate = jobPost.select("div:nth-child(1) > div:nth-child(2) > div:nth-child(2) > span:nth-child(1)").text();

                    // Split location and date
                    String[] locationAndDateSplit = jobLocationAndDate.split(" \\| ");
                    String jobDate = locationAndDateSplit[0].trim();
                    String jobLocation = locationAndDateSplit.length > 1 ? locationAndDateSplit[2].trim() : "Location not available";

                    // Create a JSON object for the job
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", "https://www.moncallcenter.ma" + jobLink);
                    jobJson.addProperty("description", jobDescription);
                    jobJson.addProperty("date", jobDate);
                    jobJson.addProperty("location", jobLocation);

                    // Add job JSON object to the array
                    jobPostsArray.add(jobJson);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Save the JSON array to a file using the JsonUtils method
        JsonUtils.saveJsonToFile(jobPostsArray, "data/MonCallCenterLinks.json");
        System.out.println("MonCallCenter scraping completed. Results saved to data/MonCallCenterLinks.json");
    }
}
