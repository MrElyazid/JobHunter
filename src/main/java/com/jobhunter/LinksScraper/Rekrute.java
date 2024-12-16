package com.jobhunter.LinksScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.IOException;

import com.jobhunter.util.JsonUtils;

public class Rekrute {

    private static final String BASE_URL = "https://www.rekrute.com/offres.html?s=1&p=";
    private static final int MAX_PAGES = 10;

    public void scrape() {
        JsonArray jobsArray = new JsonArray();

        try {
            for (int i = 1; i <= MAX_PAGES; i++) {
                String url = BASE_URL + i + "&o=1";
                Document doc = Jsoup.connect(url).get();
                
                Elements jobListings = doc.select("div.col-sm-10.col-xs-12");
                
                for (Element job : jobListings) {
                    String title = job.select("a.titreJob").text().replace("\"", "'");
                    String link = "https://www.rekrute.com" + job.select("a.titreJob").attr("href");


                    JsonObject jobJson = new JsonObject();
                    jobJson.addProperty("title", title);
                    jobJson.addProperty("link", link);

                    jobsArray.add(jobJson);
                }
            }

            JsonUtils.saveJsonToFile(jobsArray, "data/RekruteLinks.json");
            System.out.println("Rekrute scraping completed. Results saved to data/RekruteLinks.json");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
