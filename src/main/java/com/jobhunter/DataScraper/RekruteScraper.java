package com.jobhunter.DataScraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.jobhunter.util.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.FileReader;
import java.io.IOException;

public class RekruteScraper {

    private static final String LINKS_FILE = "data/RekruteLinks.json";
    private static final String OUTPUT_FILE = "lastScrappe/RekruteData.json";

    public static void main(String[] args) {
        // Create an instance of RekruteScraper and run the scrape method
        RekruteScraper scraper = new RekruteScraper();
        scraper.scrape();
    }

    public void scrape() {
        try {
            // Step 1: Read links from the JSON file
            JsonArray linksArray = JsonParser.parseReader(new FileReader(LINKS_FILE)).getAsJsonArray();

            JsonArray scrapedData = new JsonArray();

            // Step 2: Loop through each link and scrape data
            for (int i = 0; i < linksArray.size(); i++) {
                JsonObject jobLinkObject = linksArray.get(i).getAsJsonObject();
                String link = jobLinkObject.get("link").getAsString();

                // Scrape job details for the current link
                JsonObject jobDetails = scrapeJobDetails(link);

                // Add scraped job details to the output array
                scrapedData.add(jobDetails);
            }

            // Step 3: Save the scraped data to a new JSON file
            JsonUtils.saveJsonToFile(scrapedData, OUTPUT_FILE);
            System.out.println("Job details scraping completed. Results saved to " + OUTPUT_FILE);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private JsonObject scrapeJobDetails(String jobUrl) {
        JsonObject jobInfo = new JsonObject();

        try {
            // Fetch the job page HTML
            Document jobDoc = Jsoup.connect(jobUrl).get();

            // Extract job details using Jsoup selectors
            Element titleElement = jobDoc.select("h1").first();
            jobInfo.addProperty("title", (titleElement != null) ? titleElement.text() : "No title found");

            Element targetDiv = jobDoc.select("div.contentbloc").first();
            Element introToSocieteElement = targetDiv != null ? targetDiv.select("div#recruiterDescription p").first() : null;
            jobInfo.addProperty("company", (introToSocieteElement != null) ? introToSocieteElement.text() : "No company info found");

            Element RecruiterElement = jobDoc.select("#recruiterDescription").first();
            jobInfo.addProperty("recruiterDescription", (RecruiterElement != null) ? RecruiterElement.text() : "No Recruiter Description Found");

            Element PostElement = jobDoc.select("div.col-md-12:nth-child(5)").first();
            jobInfo.addProperty("postDescription", (PostElement != null) ? PostElement.text() : "No Post Description Found");

            Element ProfilElement = jobDoc.select("div.col-md-12:nth-child(6)").first();
            jobInfo.addProperty("profilDescription", (ProfilElement != null) ? ProfilElement.text() : "No Profil Description Found");

            Element expDiv = jobDoc.select("ul.featureInfo").first();
            Element educationElement = expDiv != null ? expDiv.select("li").eq(2).first() : null;
            jobInfo.addProperty("education", (educationElement != null) ? educationElement.text() : "Not found");

            Element experienceElement = expDiv != null ? expDiv.select("li").eq(0).first() : null;
            jobInfo.addProperty("experience", (experienceElement != null) ? experienceElement.text() : "Not found");

            Element contractElement = jobDoc.select("ul.featureInfo > li > span.tagContrat").eq(0).first();
            jobInfo.addProperty("contract", (contractElement != null) ? contractElement.text() : "Not found");

            jobInfo.addProperty("url", jobUrl);

        } catch (IOException e) {
            e.printStackTrace();
            jobInfo.addProperty("error", "Failed to scrape: " + jobUrl);
        }

        return jobInfo;
    }
}
