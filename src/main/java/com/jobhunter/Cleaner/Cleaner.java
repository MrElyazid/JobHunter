package com.jobhunter.Cleaner;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;

public class Cleaner {
    private static final String DATABASE_PATH = "database/database.json";

    public static void main(String[] args) throws Exception {
        File lastScrappeDir = new File("lastScrappe");
        File[] jsonFiles = lastScrappeDir.listFiles((dir, name) -> name.endsWith(".json"));
        ObjectMapper objectMapper = new ObjectMapper();

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No JSON files found in lastScrappe directory");
            return;
        }

        // Create database directory if it doesn't exist
        new File("database").mkdirs();
        
        // Create or clear database.json
        try (FileWriter writer = new FileWriter(DATABASE_PATH)) {
            writer.write("");
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (File jsonFile : jsonFiles) {
                System.out.println("Processing file: " + jsonFile.getName());
                try {
                    // Read the file as a JSON array
                    JsonNode jobOffersArray = objectMapper.readTree(new FileReader(jsonFile));
                    
                    // Ensure we have an array of job offers
                    if (!jobOffersArray.isArray()) {
                        System.out.println("Warning: " + jsonFile.getName() + " does not contain a JSON array. Skipping...");
                        continue;
                    }

                    // Process each job offer in the array
                    for (JsonNode jobOffer : jobOffersArray) {
                        System.out.println("Processing job offer from file: " + jsonFile.getName());
                        
                        String apiUrl = "https://openrouter.ai/api/v1/chat/completions";
                        String apiKey = "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";

                        Map<String, Object> body = new HashMap<>();
                        body.put("model", "openai/gpt-4o-mini-2024-07-18");

                        // Structure the message content properly
                        ArrayList<Map<String, Object>> messages = new ArrayList<>();
                        messages.add(Map.of(
                            "role", "user",
                            "content", "You are tasked with cleaning and extracting structured data from job offers in JSON format. The input consists of a JSON object containing a job offer, scraped from a website, likely in French. The data may have special character issues and missing fields, for the output dont write french special characters like é, è à ... etc, instead replace them with e, a ... etc, and dont write '. Generate a structured JSON object adhering to the schema given to you in the parameters. Here is the json job_offer: " + jobOffer.toString()
                        ));
                        body.put("messages", messages);

                        // Define the schema
                        Map<String, Object> schema = new HashMap<>();
                        schema.put("type", "object");
                        schema.put("additionalProperties", false);
                        Map<String, Object> properties = new HashMap<>();
                        properties.put("location", Map.of("type", "string", "description", "The job's location (city, region, etc.)"));
                        properties.put("sector", Map.of("type", "string", "description", "One item from the predefined sector list", "enum", new String[]{
                            "Agriculture and Agribusiness", "Tourism and Hospitality", "Information Technology (IT)", "Software Engineering",
                            "Healthcare and Medical Services", "Construction and Civil Engineering", "Automotive Industry", "Aerospace Industry",
                            "Textile and Clothing Manufacturing", "Mining (Phosphate)", "Renewable Energy", "Telecommunications",
                            "Banking and Financial Services", "Education and Teaching", "Call Center and Customer Service", "Retail and Sales",
                            "Food Processing and Manufacturing", "Logistics and Transportation", "Real Estate", "Marketing and Advertising",
                            "Pharmaceuticals", "Electronics Manufacturing", "Chemical Industry", "Fishing and Marine Resources",
                            "Business Process Outsourcing (BPO)"
                        }));
                        properties.put("job_description", Map.of("type", "string", "description", "A concise summary of the job description"));
                        properties.put("min_salary", Map.of("type", "number", "description", "Minimum salary in Moroccan dirhams (dh)"));
                        properties.put("is_remote", Map.of("type", "boolean", "description", "Boolean indicating if the job is remote"));
                        properties.put("hard_skills", Map.of("type", "array", "items", Map.of("type", "string"), "description", "List of hard skills required for the job"));
                        properties.put("soft_skills", Map.of("type", "array", "items", Map.of("type", "string"), "description", "List of soft skills required for the job"));
                        properties.put("company", Map.of("type", "string", "description", "Name of the hiring company or recruiter"));
                        properties.put("foreign_company", Map.of("type", "boolean", "description", "Boolean indicating if the company is foreign"));
                        properties.put("company_description", Map.of("type", "string", "description", "Summary of the company/recruiter description"));
                        properties.put("contract_type", Map.of("type", "string", "description", "Type of job contract (e.g., CDI, CDD)"));
                        properties.put("is_internship", Map.of("type", "boolean", "description", "Boolean indicating if the job is an internship"));
                        properties.put("source", Map.of("type", "string", "description", "Platform where the job was posted", "enum", new String[]{
                            "Khdma", "EmploiMa", "Anapec", "MonCallCenter", "Rekrute", "StagairesMa", "MarocAnnonces"
                        }));
                        properties.put("link", Map.of("type", "string", "description", "Link to the job offer"));
                        properties.put("min_experience", Map.of("type", "integer", "description", "Minimum years of experience required"));
                        properties.put("diploma", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Desired diplomas or qualifications"));
                        properties.put("title", Map.of("type", "string", "description", "Job title"));
                        properties.put("application_date", Map.of("type", "string", "format", "date", "description", "Date of application"));
                        properties.put("date_of_publication", Map.of("type", "string", "format", "date", "description", "Date of job posting publication"));
                        properties.put("company_address", Map.of("type", "string", "description", "Address of the company"));
                        properties.put("company_website", Map.of("type", "string", "description", "Website of the company"));
                        properties.put("region", Map.of("type", "string", "description", "Region of the job"));
                        properties.put("desired_profile", Map.of("type", "string", "description", "Description of the desired candidate profile"));
                        properties.put("personality_traits", Map.of("type", "string", "description", "Desired personality traits for the job"));
                        properties.put("languages", Map.of("type", "string", "description", "Required languages for the job"));
                        properties.put("language_proficiency", Map.of("type", "string", "description", "Required language proficiency levels"));
                        properties.put("recommended_skills", Map.of("type", "string", "description", "Additional recommended skills for the job"));
                        properties.put("job", Map.of("type", "string", "description", "Job category or type"));

                        schema.put("properties", properties);
                        // Include all properties in required array
                        schema.put("required", new String[]{
                            "location", "sector", "job_description", "min_salary", "is_remote",
                            "hard_skills", "soft_skills", "company", "foreign_company", "company_description",
                            "contract_type", "is_internship", "source", "link", "min_experience", "diploma",
                            "title", "application_date", "date_of_publication", "company_address", "company_website",
                            "region", "desired_profile", "personality_traits", "languages", "language_proficiency",
                            "recommended_skills", "job"
                        });

                        Map<String, Object> responseFormat = new HashMap<>();
                        responseFormat.put("type", "json_schema");
                        responseFormat.put("json_schema", Map.of(
                            "name", "job_data",
                            "strict", true,
                            "schema", schema
                        ));
                        body.put("response_format", responseFormat);

                        String jsonBody = objectMapper.writeValueAsString(body);
                        System.out.println("Request body: " + jsonBody);  // Debug print

                        HttpPost request = new HttpPost(apiUrl);
                        request.setHeader("Authorization", "Bearer " + apiKey);
                        request.setHeader("Content-Type", "application/json");
                        request.setHeader("X-Title", "JobHunter");
                        request.setEntity(new StringEntity(jsonBody));

                        try (CloseableHttpResponse response = client.execute(request)) {
                            // Read the response content
                            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                            StringBuilder responseContent = new StringBuilder();
                            String line;
                            while ((line = reader.readLine()) != null) {
                                responseContent.append(line);
                            }

                            // Parse the response
                            JsonNode responseJson = objectMapper.readTree(responseContent.toString());
                            System.out.println("Response: " + responseJson.toString());

                            // Extract the actual response content from the choices array
                            if (responseJson.has("choices") && responseJson.get("choices").size() > 0) {
                                JsonNode messageContent = responseJson.get("choices").get(0).get("message").get("content");
                                
                                // Simply append the raw LLM response to database.json
                                try (FileWriter fileWriter = new FileWriter(DATABASE_PATH, true)) {
                                    fileWriter.write(messageContent.toString() + "\n");
                                    System.out.println("Successfully wrote raw job data to database.json");
                                }
                            } else {
                                System.out.println("Error: Unexpected response format from API");
                                System.out.println("Full response: " + responseJson.toString());
                            }
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing file " + jsonFile.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }

        System.out.println("\nAll job offers have been processed. Run CleanLLM to clean and format the database.");
    }
}
