package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;

import java.io.IOException;

public class Anapec {

    private static final String BASE_URL = "https://anapec.ma/home-page-o1/chercheur-emploi/offres-demploi/?pg=";
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 0; page < MAX_PAGES; page++) { //page 0 to 9
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select("div.offres-item");
                for (Element jobPost : jobPosts) {
                    String jobLink = jobPost.select("a.w-full.p-1").attr("href");
                    String jobTitle = jobPost.select("p.offre-date.self-end.font-semibold.mb-2").text();

                    
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);

                    jobPostsArray.add(jobJson);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/AnapecLinks.json");
        System.out.println("Anapec scraping completed. Results saved to data/AnapecLinks.json");
    }
}