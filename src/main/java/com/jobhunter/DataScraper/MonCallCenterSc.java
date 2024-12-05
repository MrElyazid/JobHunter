package com.jobhunter.DataScraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class MonCallCenterSc {

    private static final String OUTPUT_FILE_PATH = "lastScrappe/MonCallCenterData.json";
    private static final String LINKS_FILE_PATH = "data/MonCallCenterLinks.json";

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
        
                    // Scrape company name
                    Element companyNameElement = doc.selectFirst("h2 a");
                    jobDetails.addProperty("company", companyNameElement != null ? companyNameElement.text() : "Unknown");
        
                    // Scrape job location and date
                    Element jobInfoElement = doc.selectFirst("h2 + span");
                    if (jobInfoElement != null) {
                        String[] jobInfoParts = jobInfoElement.text().split("\\|");
                        jobDetails.addProperty("date_posted", jobInfoParts.length > 0 ? jobInfoParts[0].trim() : "Unknown");
                        jobDetails.addProperty("location", jobInfoParts.length > 1 ? jobInfoParts[1].trim() : "Unknown");
                    }
        
                    // Scrape job description
                    Element jobDescriptionElement = doc.selectFirst(".blocpost .offredetails p");
                    jobDetails.addProperty("job_description", jobDescriptionElement != null ? jobDescriptionElement.text() : "No description available");
        
                    // Scrape qualifications
                    Element qualificationsElement = doc.selectFirst(".blocpost:contains(Profil Recherché) p");
                    jobDetails.addProperty("qualifications", qualificationsElement != null ? qualificationsElement.text() : "No qualifications available");
        
                    // Scrape advantages
                    Element advantagesElement = doc.selectFirst(".blocpost:contains(Avantages sociaux et autres) p");
                    jobDetails.addProperty("advantages", advantagesElement != null ? advantagesElement.text() : "No advantages available");
        
                    // Scrape working hours
                    Element hoursElement = doc.selectFirst(".blocpost:contains(Amplitude horaire) p");
                    jobDetails.addProperty("working_hours", hoursElement != null ? hoursElement.text() : "Unknown");
        
                    // Scrape salary
                    Element salaryElement = doc.selectFirst(".blocpost:contains(Salaire Net + primes) p");
                    jobDetails.addProperty("salary", salaryElement != null ? salaryElement.text() : "Unknown");
        
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
        MonCallCenterSc callCenterScraper = new MonCallCenterSc();
        callCenterScraper.scrapeData();
    }
}
