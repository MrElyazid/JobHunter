package com.jobhunter.scraper;

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
    private static final int END_PAGE = 11;

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
                    String location = job.select("span.location").text();
                    String description = job.select("p").text();
                    String educationLevel = job.select("div.niveauetude").text();
                    String salary = job.select("div.salary").text();
                    String date = job.select("div.time em.date").text();

                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", title);
                    jobJson.addProperty("link", "https://www.marocannonces.com/" + link);
                    jobJson.addProperty("location", location);
                    jobJson.addProperty("description", description);
                    jobJson.addProperty("educationLevel", educationLevel);
                    jobJson.addProperty("salary", salary);
                    jobJson.addProperty("date", date);
                    
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
