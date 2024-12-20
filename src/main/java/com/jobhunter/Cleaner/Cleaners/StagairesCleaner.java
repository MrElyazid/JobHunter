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
import java.text.Normalizer;

public class StagairesCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;
    private JSONArray personalityTraits;

    public StagairesCleaner() {
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
        cleanedJobOffer.put("location", cleanText(rawJobOffer.optString("location", "NA")));
        cleanedJobOffer.put("sector", extractSector(rawJobOffer));
        cleanedJobOffer.put("job_description", cleanText(rawJobOffer.optString("job_description", "NA")));
        cleanedJobOffer.put("min_salary", extractSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", new JSONArray(extractSkills(rawJobOffer, true)).toString());
        cleanedJobOffer.put("soft_skills", new JSONArray(extractSkills(rawJobOffer, false)).toString());
        cleanedJobOffer.put("company", cleanText(rawJobOffer.optString("company", "NA")));
        cleanedJobOffer.put("foriegn_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanText(extractCompanyDescription(rawJobOffer)));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "Stagaires");
        cleanedJobOffer.put("link", rawJobOffer.optString("link", "NA"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", new JSONArray(extractDiploma(rawJobOffer)).toString());
        cleanedJobOffer.put("title", cleanText(rawJobOffer.optString("title", "NA")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", LocalDate.now().toString());
        cleanedJobOffer.put("company_address", cleanText(extractCompanyAddress(rawJobOffer)));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanText(rawJobOffer.optString("profile_requirements", "NA")));
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
    
    
    private String extractSector(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "") + " " +
                           jobOffer.optString("profile_requirements", "");
        for (int i = 0; i < sectors.length(); i++) {
            if (description.toLowerCase().contains(sectors.getString(i).toLowerCase())) {
                return cleanText(sectors.getString(i));
            }
        }
        return "NA";
    }

    private float extractSalary(JSONObject jobOffer) {
        String benefits = jobOffer.optString("benefits", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:dh|dirhams|MAD)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(benefits);
        return matcher.find() ? Float.parseFloat(matcher.group(1)) : 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String workMode = jobOffer.optString("work_mode", "").toLowerCase();
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return workMode.contains("distance") || workMode.contains("remote") || 
               description.contains("teletravail") || description.contains("a distance") || 
               description.contains("remote") || description.contains("travail a domicile");
    }

    private List<String> extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("job_description", "") + " " +
                           jobOffer.optString("profile_requirements", "");
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
        String description = jobOffer.optString("job_description", "").toLowerCase() + " " +
                           jobOffer.optString("company", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("etranger") || description.contains("groupe international");
    }

    private String extractCompanyDescription(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "");
        Pattern pattern = Pattern.compile("(?:Notre entreprise|Notre société|Qui sommes-nous)[^.]*\\.");
        Matcher matcher = pattern.matcher(description);
        return matcher.find() ? cleanText(matcher.group()) : "NA";
    }

    private String extractContractType(JSONObject jobOffer) {
        String contractType = jobOffer.optString("contract_type", "").toLowerCase();
        for (int i = 0; i < contractTypes.length(); i++) {
            if (contractType.contains(contractTypes.getString(i).toLowerCase())) {
                return cleanText(contractTypes.getString(i));
            }
        }
        return "NA";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String contractType = jobOffer.optString("contract_type", "").toLowerCase();
        String title = jobOffer.optString("title", "").toLowerCase();
        return contractType.contains("stage") || title.contains("stage") || 
               title.contains("stagiaire") || title.contains("intern");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String description = jobOffer.optString("profile_requirements", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:an|annee|ans)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(description);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<String> extractDiploma(JSONObject jobOffer) {
        String description = jobOffer.optString("profile_requirements", "");
        List<String> foundDiplomas = new ArrayList<>();
        
        for (int i = 0; i < diplomaTypes.length(); i++) {
            if (description.toLowerCase().contains(diplomaTypes.getString(i).toLowerCase())) {
                foundDiplomas.add(diplomaTypes.getString(i));
            }
        }
        return foundDiplomas.isEmpty() ? List.of("NA") : foundDiplomas;
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        String location = jobOffer.optString("location", "");
        return location.isEmpty() ? "NA" : cleanText(location);
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
        String description = jobOffer.optString("profile_requirements", "");
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
        String description = jobOffer.optString("profile_requirements", "").toLowerCase();
        List<String> languages = new ArrayList<>();
        
        if (description.contains("francais")) languages.add("francais");
        if (description.contains("anglais")) languages.add("anglais");
        if (description.contains("arabe")) languages.add("arabe");
        if (description.contains("espagnol")) languages.add("espagnol");
        
        return languages.isEmpty() ? "NA" : String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("profile_requirements", "").toLowerCase();
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
        String description = jobOffer.optString("profile_requirements", "").toLowerCase();
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