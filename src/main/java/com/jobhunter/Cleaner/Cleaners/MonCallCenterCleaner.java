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

public class MonCallCenterCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;

    public MonCallCenterCleaner() {
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
        cleanedJobOffer.put("job_description", cleanDescription(rawJobOffer.optString("job_description")));
        cleanedJobOffer.put("min_salary", extractMinSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", extractSkills(rawJobOffer, true).toString());
        cleanedJobOffer.put("soft_skills", extractSkills(rawJobOffer, false).toString());
        cleanedJobOffer.put("company", rawJobOffer.optString("company"));
        cleanedJobOffer.put("foreign_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", extractCompanyDescription(rawJobOffer));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "MonCallCenter");
        cleanedJobOffer.put("link", rawJobOffer.optString("link"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", extractDiploma(rawJobOffer).toString());
        cleanedJobOffer.put("title", cleanTitle(rawJobOffer.optString("title")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractDate(rawJobOffer.optString("date_posted")));
        cleanedJobOffer.put("company_address", extractCompanyAddress(rawJobOffer));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", extractDesiredProfile(rawJobOffer));
        cleanedJobOffer.put("personality_traits", extractPersonalityTraits(rawJobOffer));
        cleanedJobOffer.put("languages", extractLanguages(rawJobOffer));
        cleanedJobOffer.put("language_proficiency", extractLanguageProficiency(rawJobOffer));
        cleanedJobOffer.put("recommended_skills", extractRecommendedSkills(rawJobOffer));
        cleanedJobOffer.put("job", extractJob(rawJobOffer));

        return cleanedJobOffer;
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ\\s()-]", "").trim();
    }

    private String cleanDescription(String description) {
        // Remove extra whitespace
        description = description.replaceAll("\\s+", " ").trim();
        // Remove special characters except punctuation
        description = description.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ.,;:!?()\\s-]", "");
        return description;
    }

    private String extractLocation(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        String[] parts = location.split("-");
        if (parts.length > 1) {
            return parts[1].trim();
        }
        return location.trim();
    }

    private String extractSector(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "");
        for (int i = 0; i < sectors.length(); i++) {
            String sector = sectors.getString(i);
            if (description.toLowerCase().contains(sector.toLowerCase())) {
                return sector;
            }
        }
        return "Non spécifié";
    }

    private float extractMinSalary(JSONObject jobOffer) {
        String salary = jobOffer.optString("salary", "");
        Pattern salaryPattern = Pattern.compile("(\\d+)");
        Matcher matcher = salaryPattern.matcher(salary);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group(1));
        }
        return 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return description.contains("télétravail") || description.contains("à distance") || description.contains("remote");
    }

    private JSONArray extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("job_description", "") + " " + jobOffer.optString("qualifications", "");
        List<String> skills = new ArrayList<>();
        JSONArray skillsToCheck = isHardSkill ? hardSkills : softSkills;

        for (int i = 0; i < skillsToCheck.length(); i++) {
            String skill = skillsToCheck.getString(i);
            Pattern skillPattern = Pattern.compile("\\b" + Pattern.quote(skill) + "\\b", Pattern.CASE_INSENSITIVE);
            Matcher matcher = skillPattern.matcher(description);
            if (matcher.find()) {
                skills.add(skill);
            }
        }

        return new JSONArray(skills);
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("étranger") || description.contains("foreign");
    }

    private String extractCompanyDescription(JSONObject jobOffer) {
        return "Non spécifié"; // Company description is not provided in the example JSON
    }

    private String extractContractType(JSONObject jobOffer) {
        String advantages = jobOffer.optString("advantages", "");
        for (int i = 0; i < contractTypes.length(); i++) {
            String contractType = contractTypes.getString(i);
            if (advantages.toLowerCase().contains(contractType.toLowerCase())) {
                return contractType;
            }
        }
        return "CDI"; // Default to CDI as mentioned in the advantages
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title", "").toLowerCase();
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") ||
               description.contains("stage") || description.contains("stagiaire");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String qualifications = jobOffer.optString("qualifications", "");
        Pattern experiencePattern = Pattern.compile("(\\d+)\\s*(?:an|année|years?|ans?)");
        Matcher matcher = experiencePattern.matcher(qualifications);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private JSONArray extractDiploma(JSONObject jobOffer) {
        String qualifications = jobOffer.optString("qualifications", "");
        List<String> diplomas = new ArrayList<>();
        for (int i = 0; i < diplomaTypes.length(); i++) {
            String diploma = diplomaTypes.getString(i);
            if (qualifications.toLowerCase().contains(diploma.toLowerCase())) {
                diplomas.add(diploma);
            }
        }
        return new JSONArray(diplomas);
    }

    private String extractDate(String dateString) {
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            LocalDate date = LocalDate.parse(dateString, inputFormatter);
            return date.toString(); // Returns in yyyy-MM-dd format
        } catch (Exception e) {
            return LocalDate.now().toString(); // Default to current date if parsing fails
        }
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        String[] parts = location.split("-");
        if (parts.length > 0) {
            return parts[0].trim();
        }
        return "Non spécifié";
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        return "Non spécifié"; // Company website is not provided in the example JSON
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

    private String extractDesiredProfile(JSONObject jobOffer) {
        return jobOffer.optString("qualifications", "Non spécifié");
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String qualifications = jobOffer.optString("qualifications", "");
        Pattern traitsPattern = Pattern.compile("(?:qualités suivantes|compétences suivantes)\\s*:(.+)");
        Matcher matcher = traitsPattern.matcher(qualifications);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Non spécifié";
    }

    private String extractLanguages(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        List<String> languages = new ArrayList<>();
        String[] languageKeywords = {"français", "anglais", "arabe", "espagnol", "allemand", "italien", "chinois"};

        for (String keyword : languageKeywords) {
            if (description.contains(keyword)) {
                languages.add(keyword);
            }
        }

        return String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        if (description.contains("courant") || description.contains("bilingue") || description.contains("natif")) {
            return "Courant";
        } else if (description.contains("intermédiaire") || description.contains("bon niveau")) {
            return "Intermédiaire";
        } else if (description.contains("basique") || description.contains("notions")) {
            return "Basique";
        }
        return "Non spécifié";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        return jobOffer.optString("qualifications", "Non spécifié");
    }

    private String extractJob(JSONObject jobOffer) {
        return jobOffer.optString("title", "Non spécifié");
    }
}
