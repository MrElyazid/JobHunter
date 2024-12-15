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
    private static final int MAX_PAGES = 4;

    public void scrape() {
        JsonArray jobPostsArray = new JsonArray();
        
        for (int page = 1; page <= MAX_PAGES; page++) {
            try {
                String url = BASE_URL + page;
                Document doc = Jsoup.connect(url).get();
                Elements jobPosts = doc.select(".ast-col-md-6");
                
                for (Element jobPost : jobPosts) {
                    String jobTitle = jobPost.select("h3.tooltip_ethiques").text();
                    String jobLink = jobPost.select("a").attr("href");
                    
                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", jobTitle);
                    jobJson.addProperty("link", jobLink);
                    
                    jobPostsArray.add(jobJson);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        JsonUtils.saveJsonToFile(jobPostsArray, "data/StagairesMaLinks.json");
        System.out.println("StagairesMa scraping completed. Results saved to data/StagairesMaLinks.json");
    }
}
