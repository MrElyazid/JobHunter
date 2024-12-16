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
    private static final int MAX_PAGES = 12;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();

        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();

                Elements jobCards = doc.select(".page-search-jobs-content .card-job");

                for (Element card : jobCards) {
                    
                    String jobLink = card.attr("data-href");
                    String jobTitle = card.select("div.card-job-detail h3 a").text();
                    
                    
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);
                    
                    jobPostsArray.add(jobJson);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        JsonUtils.saveJsonToFile(jobPostsArray, "data/EmploiMaLinks.json");
        System.out.println("EmploiMa scraping completed. Results saved to data/EmploiMaLinks.json");
    }
    public static void main(String[] args) {
        EmploiMa emploiMa = new EmploiMa();
        emploiMa.scrape();  // Calls the scraping method
    }
}
