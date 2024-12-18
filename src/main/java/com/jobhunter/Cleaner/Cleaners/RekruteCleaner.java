package com.jobhunter.Cleaner.Cleaners;

import com.jobhunter.Cleaner.interfaces.JobCleaner;
import org.json.JSONObject;
import org.json.JSONArray;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class RekruteCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;

    public RekruteCleaner() {
        try {
            // Load dictionaries
            String hardSkillsJson = Files.readString(Paths.get("src/main/resources/dictionary/hard_skills.json"));
            String softSkillsJson = Files.readString(Paths.get("src/main/resources/dictionary/soft_skills.json"));
            String regionsJson = Files.readString(Paths.get("src/main/resources/dictionary/regions.json"));
            String sectorsJson = Files.readString(Paths.get("src/main/resources/dictionary/sectors.json"));
            String contractTypesJson = Files.readString(Paths.get("src/main/resources/dictionary/contract_types.json"));
            String diplomaTypesJson = Files.readString(Paths.get("src/main/resources/dictionary/diploma_types.json"));

            hardSkills = new JSONObject(hardSkillsJson).getJSONArray("skills");
            softSkills = new JSONObject(softSkillsJson).getJSONArray("skills");
            regions = new JSONObject(regionsJson).getJSONArray("regions");
            sectors = new JSONObject(sectorsJson).getJSONArray("sectors");
            contractTypes = new JSONObject(contractTypesJson).getJSONArray("contract_types");
            diplomaTypes = new JSONObject(diplomaTypesJson).getJSONArray("diploma_types");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public JSONObject cleanJobOffer(JSONObject rawJobOffer) {
        JSONObject cleanedJobOffer = new JSONObject();

        // Extract and clean fields
        cleanedJobOffer.put("location", extractLocation(rawJobOffer));
        cleanedJobOffer.put("sector", extractSector(rawJobOffer));
        cleanedJobOffer.put("job_description", cleanDescription(rawJobOffer.optString("postDescription")));
        cleanedJobOffer.put("min_salary", extractSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", extractSkills(rawJobOffer, true).toString());
        cleanedJobOffer.put("soft_skills", extractSkills(rawJobOffer, false).toString());
        cleanedJobOffer.put("company", cleanCompanyName(rawJobOffer.optString("company")));
        cleanedJobOffer.put("foreign_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanDescription(rawJobOffer.optString("recruiterDescription")));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "Rekrute");
        cleanedJobOffer.put("link", rawJobOffer.optString("url"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", extractDiploma(rawJobOffer).toString());
        cleanedJobOffer.put("title", cleanTitle(rawJobOffer.optString("title")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractDate(rawJobOffer, "date_of_publication"));
        cleanedJobOffer.put("company_address", extractCompanyAddress(rawJobOffer));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanDescription(rawJobOffer.optString("profilDescription")));
        cleanedJobOffer.put("personality_traits", extractPersonalityTraits(rawJobOffer));
        cleanedJobOffer.put("languages", extractLanguages(rawJobOffer));
        cleanedJobOffer.put("language_proficiency", extractLanguageProficiency(rawJobOffer));
        cleanedJobOffer.put("recommended_skills", extractRecommendedSkills(rawJobOffer));
        cleanedJobOffer.put("job", extractJob(rawJobOffer));

        return cleanedJobOffer;
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[^a-zA-Z0-9\\s-()]", "").trim();
    }

    private String cleanCompanyName(String company) {
        return company.replaceAll("[^a-zA-Z0-9\\s&]", "").trim();
    }

    private String cleanDescription(String description) {
        // Remove HTML tags
        description = description.replaceAll("<[^>]*>", "");
        // Remove special characters
        description = description.replaceAll("[^a-zA-Z0-9\\s.,;:()'-]", " ");
        // Remove extra whitespace
        description = description.replaceAll("\\s+", " ").trim();
        return description;
    }

    private String extractLocation(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        if (location.isEmpty()) {
            location = jobOffer.optString("profilDescription", "");
        }
        Pattern cityPattern = Pattern.compile("\\b([A-Z][a-z]+)\\b");
        Matcher matcher = cityPattern.matcher(location);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "Non spécifié";
    }

    private String extractSector(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "") + " " + jobOffer.optString("profilDescription", "");
        for (int i = 0; i < sectors.length(); i++) {
            String sector = sectors.getString(i);
            if (description.toLowerCase().contains(sector.toLowerCase())) {
                return sector;
            }
        }
        return "Autre";
    }

    private float extractSalary(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "") + " " + jobOffer.optString("profilDescription", "");
        Pattern salaryPattern = Pattern.compile("(\\d+)\\s*(dh|dirhams|MAD)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = salaryPattern.matcher(description);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group(1));
        }
        return 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        return description.contains("remote") || description.contains("télétravail") || description.contains("a distance");
    }

    private JSONArray extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("postDescription", "") + " " + jobOffer.optString("profilDescription", "");
        List<String> skills = new ArrayList<>();
        JSONArray skillsToCheck = isHardSkill ? hardSkills : softSkills;

        for (int i = 0; i < skillsToCheck.length(); i++) {
            String skill = skillsToCheck.getString(i);
            if (description.toLowerCase().contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }

        return new JSONArray(skills);
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String companyDescription = jobOffer.optString("recruiterDescription", "").toLowerCase();
        return companyDescription.contains("international") || companyDescription.contains("multinational");
    }

    private String extractContractType(JSONObject jobOffer) {
        String contractInfo = jobOffer.optString("contract", "");
        for (int i = 0; i < contractTypes.length(); i++) {
            String contractType = contractTypes.getString(i);
            if (contractInfo.toLowerCase().contains(contractType.toLowerCase())) {
                return contractType;
            }
        }
        return "Autre";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title", "").toLowerCase();
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        return title.contains("stage") || title.contains("internship") || 
               description.contains("stage") || description.contains("internship");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String experienceInfo = jobOffer.optString("experience", "");
        Pattern experiencePattern = Pattern.compile("(\\d+)\\s*(?:an|année|ans)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = experiencePattern.matcher(experienceInfo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private JSONArray extractDiploma(JSONObject jobOffer) {
        String education = jobOffer.optString("education", "").toLowerCase();
        List<String> diplomas = new ArrayList<>();

        for (int i = 0; i < diplomaTypes.length(); i++) {
            String diploma = diplomaTypes.getString(i);
            if (education.contains(diploma.toLowerCase())) {
                diplomas.add(diploma);
            }
        }

        return new JSONArray(diplomas);
    }

    private String extractDate(JSONObject jobOffer, String dateType) {
        // Implement date extraction logic if available in the data
        return LocalDate.now().toString(); // Default to current date if not available
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        return jobOffer.optString("profilDescription", "").replaceAll("Adresse de notre siège : ", "").trim();
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        // Implement company website extraction logic if available in the data
        return "Non spécifié";
    }

    private String extractRegion(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        for (int i = 0; i < regions.length(); i++) {
            String region = regions.getString(i);
            if (location.toLowerCase().contains(region.toLowerCase())) {
                return region;
            }
        }
        return "Autre";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        List<String> traits = new ArrayList<>();
        String[] traitKeywords = {"motivated", "team player", "autonomous", "creative", "rigorous", "organized"};

        for (String keyword : traitKeywords) {
            if (description.contains(keyword)) {
                traits.add(keyword);
            }
        }

        return String.join(", ", traits);
    }

    private String extractLanguages(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        List<String> languages = new ArrayList<>();
        String[] languageKeywords = {"french", "english", "arabic", "spanish"};

        for (String keyword : languageKeywords) {
            if (description.contains(keyword)) {
                languages.add(keyword);
            }
        }

        return String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        if (description.contains("fluent") || description.contains("courant")) {
            return "Courant";
        } else if (description.contains("intermediate") || description.contains("intermédiaire")) {
            return "Intermédiaire";
        } else if (description.contains("basic") || description.contains("basique")) {
            return "Basique";
        }
        return "Non spécifié";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase() + " " + jobOffer.optString("profilDescription", "").toLowerCase();
        List<String> skills = new ArrayList<>();
        String[] skillKeywords = {"git", "agile", "scrum", "docker", "teamwork", "communication"};

        for (String keyword : skillKeywords) {
            if (description.contains(keyword)) {
                skills.add(keyword);
            }
        }

        return String.join(", ", skills);
    }

    private String extractJob(JSONObject jobOffer) {
        return jobOffer.optString("title", "Non spécifié");
    }
}
