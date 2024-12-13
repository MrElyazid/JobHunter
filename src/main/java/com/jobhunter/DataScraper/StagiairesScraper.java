package com.jobhunter.DataScraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;

public class StagiairesScraper {

    private static final String OUTPUT_FILE_PATH = "lastScrappe/StagiairesData.json";
    private static final String LINKS_FILE_PATH = "data/StagairesMaLinks.json";

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

                System.out.println("Scraping data for: " + jobTitle + " - " + jobLink);

                try {
                    // Fetch the job details page
                    Document doc = Jsoup.connect(jobLink).get();

                    // Scrape job details
                    JsonObject jobDetails = new JsonObject();
                    jobDetails.addProperty("title", jobTitle);
                    jobDetails.addProperty("link", jobLink);

                    // Scrape company name
                    Element companyNameElement = doc.selectFirst(".societe_name_single_information a");
                    jobDetails.addProperty("company", companyNameElement != null ? companyNameElement.text() : "Unknown");

                    // Scrape location, contract type, and work mode
                    Element jobInfoElement = doc.selectFirst(".ethiques_card_n_card_post");
                    if (jobInfoElement != null) {
                        String location = jobInfoElement.select("span[data-tooltip='Ville']").text();
                        String contractType = jobInfoElement.select("span[data-tooltip='Type de contrat']").text();
                        String workMode = jobInfoElement.select("span[data-tooltip='Type de lieu de travail']").text();

                        jobDetails.addProperty("location", location.isEmpty() ? "Unknown" : location);
                        jobDetails.addProperty("contract_type", contractType.isEmpty() ? "Unknown" : contractType);
                        jobDetails.addProperty("work_mode", workMode.isEmpty() ? "Unknown" : workMode);
                    }

                    // Scrape job description
                    Element jobDescriptionElement = doc.selectFirst(".body_card_single_content_offre");
                    jobDetails.addProperty("job_description", jobDescriptionElement != null ? jobDescriptionElement.text() : "No description available");

                    // Scrape profile requirements
                    Element profileElement = doc.selectFirst(".body_card_single_content_offre:contains(Profil recherché)");
                    jobDetails.addProperty("profile_requirements", profileElement != null ? profileElement.text() : "No requirements available");

                    // Scrape benefits
                    Element benefitsElement = doc.selectFirst(".body_card_single_content_offre:contains(Pourquoi rejoindre)");
                    jobDetails.addProperty("benefits", benefitsElement != null ? benefitsElement.text() : "No benefits available");

                    // Add job details to the array
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
        StagiairesScraper scraper = new StagiairesScraper();
        scraper.scrapeData();
    }
}