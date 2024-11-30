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

        for (int page = 0; page < MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.offers-boxes > div.offer-box");

                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.select("div.offer-heading > h3.offer-title > a").attr("href");
                    String jobTitle = jobPost.select("div.offer-heading > h3.offer-title > a").text();

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);

                    jobPostsArray.add(jobJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/MJobLinks.json");
        System.out.println("MJob scraping completed. Results saved to data/MJobLinks.json");
    }
}
