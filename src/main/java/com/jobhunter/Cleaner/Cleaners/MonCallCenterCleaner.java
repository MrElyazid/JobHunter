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
import java.text.Normalizer;

public class MonCallCenterCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;
    private JSONArray personalityTraits;

    public MonCallCenterCleaner() {
        try {
            // Load dictionaries
            String hardSkillsJson = Files.readString(Paths.get("src/main/resources/dictionary/hard_skills.json"));
            String softSkillsJson = Files.readString(Paths.get("src/main/resources/dictionary/soft_skills.json"));
            String regionsJson = Files.readString(Paths.get("src/main/resources/dictionary/regions.json"));
            String sectorsJson = Files.readString(Paths.get("src/main/resources/dictionary/sectors.json"));
            String contractTypesJson = Files.readString(Paths.get("src/main/resources/dictionary/contract_types.json"));
            String diplomaTypesJson = Files.readString(Paths.get("src/main/resources/dictionary/diploma_types.json"));
            String personalityTraitsJson = Files.readString(Paths.get("src/main/resources/dictionary/personality_traits.json"));

            hardSkills = new JSONObject(hardSkillsJson).getJSONArray("skills");
            softSkills = new JSONObject(softSkillsJson).getJSONArray("skills");
            regions = new JSONObject(regionsJson).getJSONArray("regions");
            sectors = new JSONObject(sectorsJson).getJSONArray("sectors");
            contractTypes = new JSONObject(contractTypesJson).getJSONArray("contract_types");
            diplomaTypes = new JSONObject(diplomaTypesJson).getJSONArray("diploma_types");
            personalityTraits = new JSONObject(personalityTraitsJson).getJSONArray("traits");

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
        cleanedJobOffer.put("job_description", cleanText(rawJobOffer.optString("job_description", "NA")));
        cleanedJobOffer.put("min_salary", extractMinSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", new JSONArray(extractSkills(rawJobOffer, true)).toString());
        cleanedJobOffer.put("soft_skills", new JSONArray(extractSkills(rawJobOffer, false)).toString());
        cleanedJobOffer.put("company", cleanText(rawJobOffer.optString("company", "NA")));
        cleanedJobOffer.put("foriegn_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanText(extractCompanyDescription(rawJobOffer)));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "MonCallCenter");
        cleanedJobOffer.put("link", rawJobOffer.optString("link", "NA"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", new JSONArray(extractDiploma(rawJobOffer)).toString());
        cleanedJobOffer.put("title", cleanText(rawJobOffer.optString("title", "NA")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractDate(rawJobOffer.optString("date_posted", "")));
        cleanedJobOffer.put("company_address", cleanText(extractCompanyAddress(rawJobOffer)));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanText(rawJobOffer.optString("qualifications", "NA")));
        cleanedJobOffer.put("personality_traits", extractPersonalityTraits(rawJobOffer));
        cleanedJobOffer.put("languages", extractLanguages(rawJobOffer));
        cleanedJobOffer.put("language_profeciency", extractLanguageProficiency(rawJobOffer));
        cleanedJobOffer.put("recommended_skills", extractRecommendedSkills(rawJobOffer));
        cleanedJobOffer.put("job", cleanText(rawJobOffer.optString("title", "NA")));

        return cleanedJobOffer;
    }

    private String cleanText(String text) {
        if (text == null || text.isEmpty() || text.equals("N/A")) return "NA";
    
        // Replace specific accented characters with their ASCII equivalents before normalization
        String preprocessed = text
            .replaceAll("é", "e")
            .replaceAll("è", "e")
            .replaceAll("ê", "e")
            .replaceAll("à", "a")
            .replaceAll("ù", "u")
            .replaceAll("ç", "c")
            .replaceAll("ô", "o")
            .replaceAll("î", "i")
            .replaceAll("ï", "i")
            .replaceAll("â", "a");
    
        // Normalize the text to NFD form (decomposes characters into base + diacritics)
        String normalized = Normalizer.normalize(preprocessed, Normalizer.Form.NFD);
    
        // Remove any remaining diacritical marks
        String replaced = normalized.replaceAll("\\p{M}", "");
    
        // Replace invalid characters with a space
        replaced = replaced.replaceAll("[^a-zA-Z0-9\\s.,;:()'-]", " ");
    
        // Normalize spaces and trim
        replaced = replaced.replaceAll("\\s+", " ").trim();
    
        return replaced.isEmpty() ? "NA" : replaced;
    }
    
    
    private String extractLocation(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        String[] parts = location.split("-");
        if (parts.length > 1) {
            return cleanText(parts[1]);
        }
        return cleanText(location);
    }

    private String extractSector(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "");
        for (int i = 0; i < sectors.length(); i++) {
            if (description.toLowerCase().contains(sectors.getString(i).toLowerCase())) {
                return cleanText(sectors.getString(i));
            }
        }
        return "NA";
    }

    private float extractMinSalary(JSONObject jobOffer) {
        String salary = jobOffer.optString("salary", "");
        Pattern pattern = Pattern.compile("(\\d+)");
        Matcher matcher = pattern.matcher(salary);
        return matcher.find() ? Float.parseFloat(matcher.group(1)) : 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return description.contains("teletravail") || description.contains("a distance") || 
               description.contains("remote") || description.contains("travail a domicile");
    }

    private List<String> extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("job_description", "") + " " + 
                           jobOffer.optString("qualifications", "");
        List<String> foundSkills = new ArrayList<>();
        JSONArray skillsToCheck = isHardSkill ? hardSkills : softSkills;

        for (int i = 0; i < skillsToCheck.length(); i++) {
            String skill = skillsToCheck.getString(i);
            if (description.toLowerCase().contains(skill.toLowerCase())) {
                foundSkills.add(skill);
            }
        }
        return foundSkills;
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("etranger") || description.contains("groupe international");
    }

    private String extractCompanyDescription(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "");
        return description.isEmpty() ? "NA" : cleanText(description);
    }

    private String extractContractType(JSONObject jobOffer) {
        String advantages = jobOffer.optString("advantages", "");
        for (int i = 0; i < contractTypes.length(); i++) {
            if (advantages.toLowerCase().contains(contractTypes.getString(i).toLowerCase())) {
                return cleanText(contractTypes.getString(i));
            }
        }
        return "NA";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title", "").toLowerCase();
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") ||
               description.contains("stage") || description.contains("stagiaire");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String qualifications = jobOffer.optString("qualifications", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:an|annee|ans)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(qualifications);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<String> extractDiploma(JSONObject jobOffer) {
        String qualifications = jobOffer.optString("qualifications", "");
        List<String> foundDiplomas = new ArrayList<>();
        
        for (int i = 0; i < diplomaTypes.length(); i++) {
            if (qualifications.toLowerCase().contains(diplomaTypes.getString(i).toLowerCase())) {
                foundDiplomas.add(diplomaTypes.getString(i));
            }
        }
        return foundDiplomas.isEmpty() ? List.of("NA") : foundDiplomas;
    }

    private String extractDate(String dateString) {
        if (dateString == null || dateString.isEmpty()) {
            return LocalDate.now().toString();
        }
        try {
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
            return LocalDate.parse(dateString, inputFormatter).toString();
        } catch (Exception e) {
            return LocalDate.now().toString();
        }
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        String[] parts = location.split("-");
        return parts.length > 0 ? cleanText(parts[0]) : "NA";
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "");
        Pattern pattern = Pattern.compile("(?:http[s]?://)?(?:www\\.)?[\\w-]+\\.[\\w.]+");
        Matcher matcher = pattern.matcher(description);
        return matcher.find() ? matcher.group() : "NA";
    }

    private String extractRegion(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        for (int i = 0; i < regions.length(); i++) {
            if (location.toLowerCase().contains(regions.getString(i).toLowerCase())) {
                return cleanText(regions.getString(i));
            }
        }
        return "NA";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "") + " " +
                           jobOffer.optString("qualifications", "");
        List<String> foundTraits = new ArrayList<>();
        
        for (int i = 0; i < personalityTraits.length(); i++) {
            String trait = personalityTraits.getString(i);
            if (description.toLowerCase().contains(trait.toLowerCase())) {
                foundTraits.add(trait);
            }
        }
        return foundTraits.isEmpty() ? "NA" : String.join(", ", foundTraits);
    }

    private String extractLanguages(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        List<String> languages = new ArrayList<>();
        
        if (description.contains("francais")) languages.add("francais");
        if (description.contains("anglais")) languages.add("anglais");
        if (description.contains("arabe")) languages.add("arabe");
        if (description.contains("espagnol")) languages.add("espagnol");
        
        return languages.isEmpty() ? "NA" : String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        if (description.contains("courant") || description.contains("bilingue")) {
            return "Courant";
        } else if (description.contains("intermediaire") || description.contains("bon niveau")) {
            return "Intermediaire";
        } else if (description.contains("debutant") || description.contains("basique")) {
            return "Debutant";
        }
        return "NA";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "").toLowerCase();
        List<String> skills = new ArrayList<>();
        String[] recommendedSkills = {"git", "agile", "scrum", "docker", "jenkins", "aws", 
                                    "azure", "linux", "windows", "office", "excel"};

        for (String skill : recommendedSkills) {
            if (description.contains(skill)) {
                skills.add(skill);
            }
        }
        return skills.isEmpty() ? "NA" : String.join(", ", skills);
    }
}
