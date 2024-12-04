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

            // Extract job title
String jobTitle = doc.selectFirst("h5.ref_offre3 span.ref_offre2") != null
? doc.select("h5.ref_offre3 span.ref_offre2").text()
: "N/A";

// Extract reference, date, and agency
Element infoOffre = doc.selectFirst("p.info_offre");
String reference = (infoOffre != null && infoOffre.selectFirst("br") != null) 
? infoOffre.html().split("Référence de l’offre:")[1].split("<br>")[0].trim()
: "N/A";
String date = (infoOffre != null && infoOffre.selectFirst("br") != null)
? infoOffre.html().split("Date :")[1].split("<br>")[0].trim()
: "N/A";
String agency = (infoOffre != null && infoOffre.selectFirst("br") != null)
? infoOffre.html().split("Agence :")[1].split("<br>")[0].trim()
: "N/A";

// Extract company description
String companyDescription = doc.selectFirst("div#oneofmine > p > span") != null
? doc.select("div#oneofmine > p > span").text()
: "N/A";

// Extract job description
String contractType = doc.selectFirst("p.ref_typecontrat span") != null
? doc.select("p.ref_typecontrat span").text()
: "N/A";
String workLocation = doc.selectFirst("p.ref_lieutravail span") != null
? doc.select("p.ref_lieutravail span").text()
: "N/A";
String jobCharacteristics = doc.selectFirst("p.ref_postechara span") != null
? doc.select("p.ref_postechara span").text()
: "N/A";

// Concatenate job description
String jobDescription = String.format(
"Type de contrat: %s; Lieu de travail: %s; Caractéristiques: %s",
contractType, workLocation, jobCharacteristics
);

// Extract profile requirements
String profileDescription = doc.selectFirst("p.profil_description span") != null
? doc.select("p.profil_description span").text()
: "N/A";
String formation = doc.selectFirst("p.profil_formation span") != null
? doc.select("p.profil_formation span").text()
: "N/A";
String experience = doc.selectFirst("p.profil_experience span") != null
? doc.select("p.profil_experience span").text()
: "N/A";
String position = doc.selectFirst("p.profil_poste span") != null
? doc.select("p.profil_poste span").text()
: "N/A";
String languages = doc.selectFirst("p.profil_langues span") != null
? doc.select("p.profil_langues span").text()
: "N/A";

// Concatenate profile requirements
String profileRequirements = String.format(
"Description: %s; Formation: %s; Expérience: %s; Poste: %s; Langues: %s",
profileDescription, formation, experience, position, languages
);


            // Add extracted data to JSON object
            jobDetails.put("title", jobTitle);
            jobDetails.put("reference", reference);
            jobDetails.put("date", date);
            jobDetails.put("agency", agency);
            jobDetails.put("company_description", companyDescription);
            jobDetails.put("job_description", jobDescription);
            jobDetails.put("profile_requirements", profileRequirements);
        } catch (IOException e) {
            System.out.println("Error connecting to URL: " + url + " - " + e.getMessage());
        } catch (Exception e) {
            System.out.println("Unexpected error scraping URL: " + url + " - " + e.getMessage());
        }
        return jobDetails;
    }

    public static void main(String[] args) {
        // Input JSON file path and output file path (modify these as needed)
        String inputFilePath = "data/AnapecLinks.json"; // Replace with the actual file path
        String outputFolderPath = "lastScrappe"; // Folder to save the output file
        String outputFileName = "anapecData.json"; // Output file name
        String outputFilePath = outputFolderPath + "/" + outputFileName;

        try {
            // Ensure the output folder exists
            java.io.File folder = new java.io.File(outputFolderPath);
            if (!folder.exists()) {
                boolean folderCreated = folder.mkdirs();
                if (folderCreated) {
                    System.out.println("Created folder: " + outputFolderPath);
                } else {
                    System.out.println("Failed to create folder: " + outputFolderPath);
                }
            }

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
                jobDetails.put("input_title", jobEntry.getString("title")); // Include title from input
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
