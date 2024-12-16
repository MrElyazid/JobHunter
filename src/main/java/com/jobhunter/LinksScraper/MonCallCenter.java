package com.jobhunter.LinksScraper;

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
    private static final int MAX_PAGES = 7;

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

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", "https://www.moncallcenter.ma" + jobLink);


                    jobPostsArray.add(jobJson);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/MonCallCenterLinks.json");
        System.out.println("MonCallCenter scraping completed. Results saved to data/MonCallCenterLinks.json");
    }
}
