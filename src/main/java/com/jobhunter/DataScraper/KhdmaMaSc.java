package com.jobhunter.DataScraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


import java.io.IOException;

public class KhdmaMaSc {

    private static final String LINKS_FILE_PATH = "data/KhdmaLinks.json";
    private static final String OUTPUT_FILE_PATH = "lastScrappe/KhdmaMaData.json";

    public void scrapeData() {
        try {
            // Load the links from the JSON file
            JsonArray jobLinksArray = JsonUtils.readJsonArrayFromFile(LINKS_FILE_PATH);

            // Prepare a JSON array to store job data
            JsonArray jobDataArray = new JsonArray();

            // Loop through each link and scrape details
            for (int i = 0; i < jobLinksArray.size(); i++) {
                JsonObject jobLinkObj = jobLinksArray.get(i).getAsJsonObject();
                String jobLink = jobLinkObj.get("link").getAsString();
                String jobTitle = jobLinkObj.get("title").getAsString();

                System.out.println("Scraping data for: " + jobTitle + " - " + jobLink); // Log each scraping attempt

                try {
                    // Fetch the job details page
                    Document doc = Jsoup.connect(jobLink).get();

                    // Scrape job details
                    JsonObject jobDetails = new JsonObject();
                    jobDetails.addProperty("title", jobTitle);
                    jobDetails.addProperty("link", jobLink);

                    // Scrape job description
                    Element jobDescriptionElement = doc.selectFirst("div.eleven:nth-child(2) > div:nth-child(1) > div:nth-child(2)");
                    jobDetails.addProperty("description", jobDescriptionElement != null ? jobDescriptionElement.text() : "No description available");

                    // Scrape company name
                    Element companyElement = doc.selectFirst(".content > h4:nth-child(1)");
                    jobDetails.addProperty("company", companyElement != null ? companyElement.text().trim() : "Unknown");

                    // Scrape posting date
                    Element dateElement = doc.selectFirst(".listing-date");
                    jobDetails.addProperty("date", dateElement != null ? dateElement.text() : "Unknown");

                    // Add the job details to the array
                    jobDataArray.add(jobDetails);

                } catch (IOException e) {
                    System.err.println("Error fetching details for job: " + jobTitle + " (" + jobLink + ")");
                    e.printStackTrace();
                }
            }

            // Save the scraped job data to a JSON file
            JsonUtils.saveJsonToFile(jobDataArray, OUTPUT_FILE_PATH);
            System.out.println("Job data scraping completed. Results saved to " + OUTPUT_FILE_PATH);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        KhdmaMaSc khdmaMaSc = new KhdmaMaSc();
        khdmaMaSc.scrapeData();  // Calls the scraping method
    }
}
