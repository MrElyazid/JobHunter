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

public class MarocAnnoncesCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;

    public MarocAnnoncesCleaner() {
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
        cleanedJobOffer.put("location", rawJobOffer.optString("city"));
        cleanedJobOffer.put("sector", extractSector(rawJobOffer));
        cleanedJobOffer.put("job_description", cleanDescription(rawJobOffer.optString("description")));
        cleanedJobOffer.put("min_salary", extractMinSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", extractSkills(rawJobOffer, true).toString());
        cleanedJobOffer.put("soft_skills", extractSkills(rawJobOffer, false).toString());
        cleanedJobOffer.put("company", extractCompany(rawJobOffer));
        cleanedJobOffer.put("foreign_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", extractCompanyDescription(rawJobOffer));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "MarocAnnonces");
        cleanedJobOffer.put("link", rawJobOffer.optString("link"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", extractDiploma(rawJobOffer).toString());
        cleanedJobOffer.put("title", cleanTitle(rawJobOffer.optString("title")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractDate(rawJobOffer.optString("date")));
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
        // Remove HTML tags
        description = description.replaceAll("<[^>]*>", "");
        // Remove extra whitespace
        description = description.replaceAll("\\s+", " ").trim();
        // Remove special characters except punctuation
        description = description.replaceAll("[^a-zA-Z0-9àâçéèêëîïôûùüÿñæœ.,;:!?()\\s-]", "");
        return description;
    }

    private String extractDate(String dateString) {
        Pattern datePattern = Pattern.compile("Publiée le: (\\d{2} \\w{3}-\\d{2}:\\d{2})");
        Matcher matcher = datePattern.matcher(dateString);
        if (matcher.find()) {
            String extractedDate = matcher.group(1);
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd MMM-yy:mm");
            LocalDate date = LocalDate.parse(extractedDate, inputFormatter);
            return date.toString(); // Returns in yyyy-MM-dd format
        }
        return LocalDate.now().toString(); // Default to current date if parsing fails
    }

    private String extractSector(JSONObject jobOffer) {
        String info = jobOffer.optString("info");
        Pattern sectorPattern = Pattern.compile("Domaine : ([^;]+)");
        Matcher matcher = sectorPattern.matcher(info);
        if (matcher.find()) {
            String extractedSector = matcher.group(1).trim();
            for (int i = 0; i < sectors.length(); i++) {
                String sector = sectors.getString(i);
                if (extractedSector.toLowerCase().contains(sector.toLowerCase())) {
                    return sector;
                }
            }
            return extractedSector;
        }
        return "Non spécifié";
    }

    private String extractContractType(JSONObject jobOffer) {
        String info = jobOffer.optString("info");
        Pattern contractPattern = Pattern.compile("Contrat : ([^;]+)");
        Matcher matcher = contractPattern.matcher(info);
        if (matcher.find()) {
            String extractedContract = matcher.group(1).trim();
            for (int i = 0; i < contractTypes.length(); i++) {
                String contractType = contractTypes.getString(i);
                if (extractedContract.equalsIgnoreCase(contractType)) {
                    return contractType;
                }
            }
            return extractedContract;
        }
        return "Non spécifié";
    }

    private float extractMinSalary(JSONObject jobOffer) {
        String info = jobOffer.optString("info");
        Pattern salaryPattern = Pattern.compile("Salaire : (\\d+)");
        Matcher matcher = salaryPattern.matcher(info);
        if (matcher.find()) {
            return Float.parseFloat(matcher.group(1));
        }
        return 0.0f;
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String description = jobOffer.optString("description");
        Pattern experiencePattern = Pattern.compile("Expérience (?:de |d'|: )(\\d+)");
        Matcher matcher = experiencePattern.matcher(description);
        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return 0;
    }

    private JSONArray extractDiploma(JSONObject jobOffer) {
        String info = jobOffer.optString("info");
        Pattern educationPattern = Pattern.compile("Niveau d'études : ([^;]+)");
        Matcher matcher = educationPattern.matcher(info);
        List<String> diplomas = new ArrayList<>();
        if (matcher.find()) {
            String educationLevel = matcher.group(1).trim();
            for (int i = 0; i < diplomaTypes.length(); i++) {
                String diploma = diplomaTypes.getString(i);
                if (educationLevel.toLowerCase().contains(diploma.toLowerCase())) {
                    diplomas.add(diploma);
                }
            }
        }
        return new JSONArray(diplomas);
    }

    private String extractLanguages(JSONObject jobOffer) {
        String description = jobOffer.optString("description").toLowerCase();
        List<String> languages = new ArrayList<>();
        String[] languageKeywords = {"français", "anglais", "arabe", "espagnol", "allemand", "italien", "chinois"};

        for (String keyword : languageKeywords) {
            if (description.contains(keyword)) {
                languages.add(keyword);
            }
        }

        return String.join(", ", languages);
    }

    private JSONArray extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("description");
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

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("description").toLowerCase();
        return description.contains("télétravail") || description.contains("à distance") || description.contains("remote");
    }

    private String extractCompany(JSONObject jobOffer) {
        // Implement logic to extract company name
        return "Non spécifié";
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String description = jobOffer.optString("description").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("étranger") || description.contains("foreign");
    }

    private String extractCompanyDescription(JSONObject jobOffer) {
        // Implement logic to extract company description
        return "Non spécifié";
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        // Implement logic to extract company address
        return "Non spécifié";
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        // Implement logic to extract company website
        return "Non spécifié";
    }

    private String extractRegion(JSONObject jobOffer) {
        String city = jobOffer.optString("city");
        for (int i = 0; i < regions.length(); i++) {
            String region = regions.getString(i);
            if (city.toLowerCase().contains(region.toLowerCase())) {
                return region;
            }
        }
        return "Autre";
    }

    private String extractDesiredProfile(JSONObject jobOffer) {
        // Implement logic to extract desired profile
        return "Non spécifié";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        // Implement logic to extract personality traits
        return "Non spécifié";
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("description").toLowerCase();
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
        // Implement logic to extract recommended skills
        return "Non spécifié";
    }

    private String extractJob(JSONObject jobOffer) {
        String info = jobOffer.optString("info");
        Pattern functionPattern = Pattern.compile("Fonction : ([^;]+)");
        Matcher matcher = functionPattern.matcher(info);
        if (matcher.find()) {
            return matcher.group(1).trim();
        }
        return "Non spécifié";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title").toLowerCase();
        String description = jobOffer.optString("description").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") ||
               description.contains("stage") || description.contains("stagiaire");
    }
}
