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
    public static void main(String[] args) throws Exception {
        File lastScrappeDir = new File("lastScrappe");
        File[] jsonFiles = lastScrappeDir.listFiles((dir, name) -> name.endsWith(".json"));
        ObjectMapper objectMapper = new ObjectMapper();

        if (jsonFiles == null || jsonFiles.length == 0) {
            System.out.println("No JSON files found in lastScrappe directory");
            return;
        }

        try (CloseableHttpClient client = HttpClients.createDefault()) {
            for (File jsonFile : jsonFiles) {
                System.out.println("Processing file: " + jsonFile.getName());
                try {
                    JsonNode jobOffer = objectMapper.readTree(new FileReader(jsonFile));
                    String apiUrl = "https://openrouter.ai/api/v1/chat/completions";
                    String apiKey = "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";

                    Map<String, Object> body = new HashMap<>();
                    body.put("model", "openai/gpt-4o-mini-2024-07-18");

                    // Structure the message content properly
                    ArrayList<Map<String, Object>> messages = new ArrayList<>();
                    messages.add(Map.of(
                        "role", "user",
                        "content", "You are tasked with cleaning and extracting structured data from job offers in JSON format. The input consists of a JSON object containing a job offer, scraped from a website, likely in French. The data may have special character issues and missing fields. Generate a structured JSON object adhering to the schema given to you in the parameters. Here is the json job_offer: " + jobOffer.toString()
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
                    properties.put("min_salary", Map.of("type", "integer", "description", "Minimum salary in Moroccan dirhams (dh)"));
                    properties.put("is_remote", Map.of("type", "boolean", "description", "Boolean indicating if the job is remote"));
                    properties.put("soft_skills", Map.of("type", "array", "items", Map.of("type", "string"), "description", "List of soft skills required for the job"));
                    properties.put("hard_skills", Map.of("type", "array", "items", Map.of("type", "string"), "description", "List of hard skills required for the job"));
                    properties.put("company", Map.of("type", "string", "description", "Name of the hiring company or recruiter"));
                    properties.put("company_description", Map.of("type", "string", "description", "Summary of the company/recruiter description"));
                    properties.put("contract_type", Map.of("type", "string", "description", "Type of job contract (e.g., CDI, CDD)"));
                    properties.put("foreign_company", Map.of("type", "boolean", "description", "Boolean indicating if the company is foreign"));
                    properties.put("is_internship", Map.of("type", "boolean", "description", "Boolean indicating if the job is an internship"));
                    properties.put("source", Map.of("type", "string", "description", "Platform where the job was posted", "enum", new String[]{
                        "Khdma", "EmploiMa", "Anapec", "MonCallCenter", "Rekrute", "StagairesMa", "MarocAnnonces"
                    }));
                    properties.put("link", Map.of("type", "string", "description", "Link to the job offer"));
                    properties.put("min_experience", Map.of("type", "integer", "description", "Minimum years of experience required"));
                    properties.put("diploma", Map.of("type", "array", "items", Map.of("type", "string"), "description", "Desired diplomas or qualifications"));

                    schema.put("properties", properties);
                    // Include all properties in required array
                    schema.put("required", new String[]{
                        "location", "sector", "job_description", "min_salary", "is_remote",
                        "soft_skills", "hard_skills", "company", "company_description",
                        "contract_type", "foreign_company", "is_internship", "source",
                        "link", "min_experience", "diploma"
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
                            
                            // Append the cleaned job data to database.json
                            try (FileWriter fileWriter = new FileWriter("database/database.json", true)) {
                                fileWriter.write(messageContent.toString() + "\n");
                                System.out.println("Successfully wrote job data to database.json");
                            }
                        } else {
                            System.out.println("Error: Unexpected response format from API");
                            System.out.println("Full response: " + responseJson.toString());
                        }
                    }
                } catch (Exception e) {
                    System.out.println("Error processing file " + jsonFile.getName() + ": " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }
}