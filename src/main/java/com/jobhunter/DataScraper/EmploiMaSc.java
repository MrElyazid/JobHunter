package com.jobhunter.DataScraper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.jobhunter.util.JsonUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class EmploiMaSc {

    private static final String LINKS_FILE_PATH = "data/EmploiMaLinks.json";
    private static final String OUTPUT_FILE_PATH = "lastScrappe/EmploiMaData.json";

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
                    Element companyNameElement = doc.selectFirst(".card-block-company h3 a");
                    jobDetails.addProperty("company", companyNameElement != null ? companyNameElement.text() : "Unknown");

                    // Scrape company description
                    Element companyDescriptionElement = doc.selectFirst(".company-description .truncated-text");
                    jobDetails.addProperty("company_description", companyDescriptionElement != null ? companyDescriptionElement.text() : "No description available");

                    // Scrape company sector
                    Element sectorElement = doc.selectFirst(".card-block-company li strong:contains(Secteur d´activité)");
                    jobDetails.addProperty("company_sector", sectorElement != null ? sectorElement.nextElementSibling().text() : "Unknown");

                    // Scrape company website
                    Element websiteElement = doc.selectFirst(".card-block-company li a[href^='http']");
                    jobDetails.addProperty("company_website", websiteElement != null ? websiteElement.attr("href") : "No website");

                    // Scrape job description
                    Element jobDescriptionElement = doc.selectFirst(".job-description");
                    jobDetails.addProperty("job_description", jobDescriptionElement != null ? jobDescriptionElement.text() : "No description available");

                    // Scrape job qualifications
                    Element qualificationsElement = doc.selectFirst(".job-qualifications");
                    jobDetails.addProperty("job_qualifications", qualificationsElement != null ? qualificationsElement.text() : "No qualifications available");

                    // Scrape job criteria
                    JsonObject jobCriteria = new JsonObject();
                    Elements criteriaList = doc.select(".arrow-list li");
                    for (Element detail : criteriaList) {
                        String key = detail.select("strong").text().trim();
                        String value = detail.select("span").text().trim();
                        if (!key.isEmpty() && !value.isEmpty()) {
                            jobCriteria.addProperty(key, value);
                        }
                    }
                    jobDetails.add("job_criteria", jobCriteria);

                    // Scrape the skills
                    JsonArray skillsArray = new JsonArray();
                    Elements skillsList = doc.select(".skills li");
                    for (Element skill : skillsList) {
                        skillsArray.add(skill.text());
                    }
                    jobDetails.add("skills", skillsArray);

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
        EmploiMaSc emploiMaSc = new EmploiMaSc();
        emploiMaSc.scrapeData();  // Calls the scraping method
    }
}
