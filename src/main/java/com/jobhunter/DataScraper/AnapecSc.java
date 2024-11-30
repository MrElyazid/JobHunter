package com.jobhunter.DataScraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.json.JSONArray;
import org.json.JSONObject;

import java.nio.file.Files;
import java.nio.file.Paths;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class AnapecSc {

    // Method to scrape data from a single job offer page
    private static JSONObject scrapeJobDetails(String url) {
        JSONObject jobDetails = new JSONObject();
        try {
            Document doc = Jsoup.connect(url).get();
            Element mainDiv = doc.getElementById("oneofmine");

            if (mainDiv != null) {
                // Scrape description de l'entreprise
                String descEntreprise = mainDiv.select("#oneofmine > p:nth-child(3) > span:nth-child(1)").text();

                // Scrape description de poste
                String typeDeContrat = mainDiv.select("#oneofmine > p:nth-child(6)").text();
                String lieuDeTravail = mainDiv.select("#oneofmine > p:nth-child(7)").text();
                String descPoste = typeDeContrat + "; " + lieuDeTravail;

                // Scrape profil recherché
                String descProfil = mainDiv.select("#oneofmine > p:nth-child(9)").text();
                String formation = mainDiv.select("#oneofmine > p:nth-child(10)").text();
                String langues = mainDiv.select("#oneofmine > p:nth-child(11)").text();
                String profilRecherche = descProfil + "; " + formation + "; " + langues;

                // Add to JSON object
                jobDetails.put("desc_entreprise", descEntreprise);
                jobDetails.put("desc_poste", descPoste);
                jobDetails.put("profil_recherche", profilRecherche);
            } else {
                System.out.println("No job details found on page: " + url);
            }
        } catch (IOException e) {
            System.out.println("Error connecting to URL: " + url + " - " + e.getMessage());
        }
        return jobDetails;
    }

    public static void main(String[] args) {
        // Input JSON file path and output file path (modify these as needed)
        String inputFilePath = "data\\AnapecLinks.json"; // Replace with the actual file path
        String outputFilePath = "job_details.json"; // Replace with the actual file path

        try (FileReader reader = new FileReader(inputFilePath)) {
            // Read and parse JSON input
            String content = new String(Files.readAllBytes(Paths.get(inputFilePath)));
            JSONArray jobLinksArray = new JSONArray(content);
            JSONArray results = new JSONArray();

            // Iterate over job links
            for (int i = 0; i < jobLinksArray.length(); i++) {
                JSONObject jobEntry = jobLinksArray.getJSONObject(i);
                String link = jobEntry.getString("link");
                System.out.println("Scraping job: " + link);

                // Scrape data for each job and add to results
                JSONObject jobDetails = scrapeJobDetails(link);
                jobDetails.put("title", jobEntry.getString("title")); // Include title from input
                jobDetails.put("link", link);
                results.put(jobDetails);
            }

            // Write results to output file
            try (FileWriter writer = new FileWriter(outputFilePath)) {
                writer.write(results.toString(4)); // Pretty print with 4 spaces
                System.out.println("Job details saved to: " + outputFilePath);
            }

        } catch (Exception e) {
            System.out.println("Error processing file: " + e.getMessage());
        }
    }
}
