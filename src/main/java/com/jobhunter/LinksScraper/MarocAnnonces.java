package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.io.IOException;

import com.jobhunter.util.JsonUtils;

public class MarocAnnonces {

    private static final String BASE_URL = "https://www.marocannonces.com/categorie/309/Emploi/Offres-emploi/";
    private static final int START_PAGE = 2;
    private static final int END_PAGE = 10;

    public void scrape() {
        JsonArray jobsArray = new JsonArray();

        try {
            for (int i = START_PAGE; i <= END_PAGE; i++) {
                String url = BASE_URL + i + ".html";
                Document doc = Jsoup.connect(url).get();
                
                Elements jobListings = doc.select("div.content_box ul.cars-list li");
                
                for (Element job : jobListings) {
                    String title = job.select("a").attr("title");
                    String link = job.select("a").attr("href");


                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", title);
                    jobJson.addProperty("link", "https://www.marocannonces.com/" + link);

                    
                    jobsArray.add(jobJson);
                }
            }

            JsonUtils.saveJsonToFile(jobsArray, "data/MarocAnnoncesLinks.json");
            System.out.println("MarocAnnonces scraping completed. Results saved to data/MarocAnnoncesLinks.json");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
