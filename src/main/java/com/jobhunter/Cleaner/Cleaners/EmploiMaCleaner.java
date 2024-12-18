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

public class EmploiMaCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;

    public EmploiMaCleaner() {
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
        cleanedJobOffer.put("sector", extractSector(rawJobOffer.optString("sector")));
        cleanedJobOffer.put("job_description", cleanDescription(rawJobOffer.optString("job_description")));
        cleanedJobOffer.put("min_salary", extractSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", extractHardSkills(rawJobOffer).toString());
        cleanedJobOffer.put("soft_skills", extractSoftSkills(rawJobOffer).toString());
        cleanedJobOffer.put("company", cleanCompanyName(rawJobOffer.optString("company")));
        cleanedJobOffer.put("foreign_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanDescription(rawJobOffer.optString("company_description")));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer.optJSONObject("job_criteria")));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "EmploiMa");
        cleanedJobOffer.put("link", rawJobOffer.optString("link"));
        cleanedJobOffer.put("min_experience", extractExperience(rawJobOffer.optJSONObject("job_criteria")));
        cleanedJobOffer.put("diploma", extractDiploma(rawJobOffer.optJSONObject("job_criteria")).toString());
        cleanedJobOffer.put("title", cleanTitle(rawJobOffer.optString("title")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractPublicationDate(rawJobOffer));
        cleanedJobOffer.put("company_address", extractCompanyAddress(rawJobOffer));
        cleanedJobOffer.put("company_website", extractWebsite(rawJobOffer.optString("company_website")));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer.optJSONObject("job_criteria")));
        cleanedJobOffer.put("desired_profile", extractDesiredProfile(rawJobOffer));
        cleanedJobOffer.put("personality_traits", extractPersonalityTraits(rawJobOffer));
        cleanedJobOffer.put("languages", extractLanguages(rawJobOffer.optJSONObject("job_criteria")));
        cleanedJobOffer.put("language_proficiency", extractLanguageProficiency(rawJobOffer));
        cleanedJobOffer.put("recommended_skills", extractRecommendedSkills(rawJobOffer));
        cleanedJobOffer.put("job", extractJob(rawJobOffer));

        return cleanedJobOffer;
    }

    private String cleanTitle(String title) {
        return title.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ\\s-()]", "").trim();
    }

    private String cleanCompanyName(String company) {
        return company.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ\\s&]", "").trim();
    }

    private String cleanDescription(String description) {
        // Remove HTML tags
        description = description.replaceAll("<[^>]*>", "");
        // Remove extra whitespace
        description = description.replaceAll("\\s+", " ").trim();
        // Remove special characters except punctuation
        description = description.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ.,;:!?()\\s-]", "");
        return description;
    }

    private String extractSector(String sector) {
        for (int i = 0; i < sectors.length(); i++) {
            String s = sectors.getString(i);
            if (sector.toLowerCase().contains(s.toLowerCase())) {
                return s;
            }
        }
        return "Autre";
    }

    private String extractWebsite(String website) {
        Pattern urlPattern = Pattern.compile("(https?://(?:www\\.)?[\\w-]+\\.\\w{2,})");
        Matcher matcher = urlPattern.matcher(website);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return website;
    }

    private String extractLocation(JSONObject jobOffer) {
        JSONObject jobCriteria = jobOffer.optJSONObject("job_criteria");
        if (jobCriteria != null) {
            String ville = jobCriteria.optString("Ville");
            if (!ville.isEmpty()) {
                return ville;
            }
        }
        return "Non spécifié";
    }

    private float extractSalary(JSONObject jobOffer) {
        JSONObject jobCriteria = jobOffer.optJSONObject("job_criteria");
        if (jobCriteria != null) {
            String salaryRange = jobCriteria.optString("Salaire proposé");
            Pattern salaryPattern = Pattern.compile("(\\d+)\\s*-\\s*(\\d+)\\s*DH");
            Matcher matcher = salaryPattern.matcher(salaryRange);
            if (matcher.find()) {
                return Float.parseFloat(matcher.group(1));
            }
        }
        return 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        JSONObject jobCriteria = jobOffer.optJSONObject("job_criteria");
        if (jobCriteria != null) {
            return jobCriteria.optString("Travail à distance").equalsIgnoreCase("Oui");
        }
        return false;
    }

    private JSONArray extractHardSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description") + " " + jobOffer.optString("job_qualifications");
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < hardSkills.length(); i++) {
            String skill = hardSkills.getString(i);
            if (description.toLowerCase().contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        return new JSONArray(skills);
    }

    private JSONArray extractSoftSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description") + " " + jobOffer.optString("job_qualifications");
        List<String> skills = new ArrayList<>();
        for (int i = 0; i < softSkills.length(); i++) {
            String skill = softSkills.getString(i);
            if (description.toLowerCase().contains(skill.toLowerCase())) {
                skills.add(skill);
            }
        }
        return new JSONArray(skills);
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String companyDescription = jobOffer.optString("company_description", "").toLowerCase();
        return companyDescription.contains("international") || companyDescription.contains("multinational") ||
               companyDescription.contains("internationale") || companyDescription.contains("multinationale");
    }

    private String extractContractType(JSONObject jobCriteria) {
        String contractInfo = jobCriteria.optString("Type de contrat", "");
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
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return title.contains("stage") || title.contains("internship") || 
               description.contains("stage") || description.contains("internship");
    }

    private int extractExperience(JSONObject jobCriteria) {
        String experienceInfo = jobCriteria.optString("Niveau d'expérience", "");
        Pattern experiencePattern = Pattern.compile("(\\d+)\\s*(?:an|année|years?|ans?)");
        Matcher matcher = experiencePattern.matcher(experienceInfo);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private JSONArray extractDiploma(JSONObject jobCriteria) {
        String educationInfo = jobCriteria.optString("Niveau d'études", "");
        List<String> diplomas = new ArrayList<>();
        for (int i = 0; i < diplomaTypes.length(); i++) {
            String diploma = diplomaTypes.getString(i);
            if (educationInfo.toLowerCase().contains(diploma.toLowerCase())) {
                diplomas.add(diploma);
            }
        }
        return new JSONArray(diplomas);
    }

    private String extractRegion(JSONObject jobCriteria) {
        String location = jobCriteria.optString("Région", "");
        for (int i = 0; i < regions.length(); i++) {
            String region = regions.getString(i);
            if (location.toLowerCase().contains(region.toLowerCase())) {
                return region;
            }
        }
        return "Autre";
    }

    private String extractLanguages(JSONObject jobCriteria) {
        String languagesInfo = jobCriteria.optString("Langues exigées", "");
        List<String> languages = new ArrayList<>();
        String[] languageKeywords = {"français", "anglais", "arabe", "espagnol", "allemand", "italien", "chinois"};
        for (String language : languageKeywords) {
            if (languagesInfo.toLowerCase().contains(language)) {
                languages.add(language);
            }
        }
        return String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("job_qualifications", "").toLowerCase();
        if (description.contains("courant") || description.contains("bilingue") || description.contains("natif")) {
            return "Courant";
        } else if (description.contains("intermédiaire") || description.contains("bon niveau")) {
            return "Intermédiaire";
        } else if (description.contains("basique") || description.contains("notions")) {
            return "Basique";
        }
        return "Non spécifié";
    }

    private String extractPublicationDate(JSONObject jobOffer) {
        // Implement logic to extract publication date
        // For now, we'll use the current date as a placeholder
        return LocalDate.now().toString();
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        // Implement logic to extract company address
        return "Non spécifié";
    }

    private String extractDesiredProfile(JSONObject jobOffer) {
        // Implement logic to extract desired profile
        return jobOffer.optString("job_qualifications", "Non spécifié");
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        // Implement logic to extract personality traits
        return "Non spécifié";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        // Implement logic to extract recommended skills
        JSONArray skills = jobOffer.optJSONArray("skills");
        if (skills != null) {
            return skills.toString();
        }
        return "[]";
    }

    private String extractJob(JSONObject jobOffer) {
        // Implement logic to extract job category or type
        JSONObject jobCriteria = jobOffer.optJSONObject("job_criteria");
        if (jobCriteria != null) {
            return jobCriteria.optString("Métier", "Non spécifié");
        }
        return "Non spécifié";
    }
}
