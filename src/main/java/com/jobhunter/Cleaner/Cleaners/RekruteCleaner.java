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

public class RekruteCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;
    private JSONArray personalityTraits;

    public RekruteCleaner() {
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
        cleanedJobOffer.put("job_description", cleanText(rawJobOffer.optString("postDescription", "NA")));
        cleanedJobOffer.put("min_salary", extractSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", new JSONArray(extractSkills(rawJobOffer, true)).toString());
        cleanedJobOffer.put("soft_skills", new JSONArray(extractSkills(rawJobOffer, false)).toString());
        cleanedJobOffer.put("company", cleanText(rawJobOffer.optString("company", "NA")));
        cleanedJobOffer.put("foriegn_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanText(rawJobOffer.optString("recruiterDescription", "NA")));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "Rekrute");
        cleanedJobOffer.put("link", rawJobOffer.optString("url", "NA"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", new JSONArray(extractDiploma(rawJobOffer)).toString());
        cleanedJobOffer.put("title", cleanText(rawJobOffer.optString("title", "NA")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", LocalDate.now().toString());
        cleanedJobOffer.put("company_address", cleanText(extractCompanyAddress(rawJobOffer)));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanText(rawJobOffer.optString("profilDescription", "NA")));
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
        String location = jobOffer.optString("profilDescription", "");
        Pattern cityPattern = Pattern.compile("(?i)\\b(casablanca|rabat|marrakech|tanger|fes|meknes|agadir|tetouan|oujda|kenitra|mohammedia|el jadida|nador|safi|sale|taza)\\b");
        Matcher matcher = cityPattern.matcher(location.toLowerCase());
        return matcher.find() ? cleanText(matcher.group(1)) : "NA";
    }

    private String extractSector(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "") + " " + jobOffer.optString("profilDescription", "");
        for (int i = 0; i < sectors.length(); i++) {
            if (description.toLowerCase().contains(sectors.getString(i).toLowerCase())) {
                return cleanText(sectors.getString(i));
            }
        }
        return "NA";
    }

    private float extractSalary(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "") + " " + jobOffer.optString("profilDescription", "");
        Pattern salaryPattern = Pattern.compile("(\\d+)\\s*(?:dh|dirhams|mad)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = salaryPattern.matcher(description);
        return matcher.find() ? Float.parseFloat(matcher.group(1)) : 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase();
        return description.contains("remote") || description.contains("teletravail") || 
               description.contains("a distance") || description.contains("travail a domicile");
    }

    private List<String> extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("postDescription", "") + " " + 
                           jobOffer.optString("profilDescription", "");
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
        String description = jobOffer.optString("recruiterDescription", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("etranger") || description.contains("groupe international");
    }

    private String extractContractType(JSONObject jobOffer) {
        String contract = jobOffer.optString("contract", "").toLowerCase();
        for (int i = 0; i < contractTypes.length(); i++) {
            String type = contractTypes.getString(i).toLowerCase();
            if (contract.contains(type)) {
                return cleanText(contractTypes.getString(i));
            }
        }
        return "NA";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title", "").toLowerCase();
        String description = jobOffer.optString("postDescription", "").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") || 
               description.contains("stage") || description.contains("stagiaire");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String experience = jobOffer.optString("experience", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:an|annee|ans)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(experience);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<String> extractDiploma(JSONObject jobOffer) {
        String education = jobOffer.optString("education", "").toLowerCase();
        List<String> foundDiplomas = new ArrayList<>();
        
        for (int i = 0; i < diplomaTypes.length(); i++) {
            String diploma = diplomaTypes.getString(i).toLowerCase();
            if (education.contains(diploma)) {
                foundDiplomas.add(diplomaTypes.getString(i));
            }
        }
        return foundDiplomas.isEmpty() ? List.of("NA") : foundDiplomas;
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        String profile = jobOffer.optString("profilDescription", "");
        Pattern addressPattern = Pattern.compile("(?i)Adresse[^:]*:\\s*([^\\n]+)");
        Matcher matcher = addressPattern.matcher(profile);
        return matcher.find() ? cleanText(matcher.group(1)) : "NA";
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        String description = jobOffer.optString("recruiterDescription", "");
        Pattern websitePattern = Pattern.compile("www\\.[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        Matcher matcher = websitePattern.matcher(description);
        return matcher.find() ? matcher.group() : "NA";
    }

    private String extractRegion(JSONObject jobOffer) {
        String location = jobOffer.optString("profilDescription", "");
        for (int i = 0; i < regions.length(); i++) {
            if (location.toLowerCase().contains(regions.getString(i).toLowerCase())) {
                return cleanText(regions.getString(i));
            }
        }
        return "NA";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "") + " " + 
                           jobOffer.optString("profilDescription", "");
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
        String description = jobOffer.optString("postDescription", "").toLowerCase();
        List<String> languages = new ArrayList<>();
        String[] languageKeywords = {"francais", "anglais", "arabe", "espagnol"};

        for (String lang : languageKeywords) {
            if (description.contains(lang)) {
                languages.add(lang);
            }
        }
        return languages.isEmpty() ? "NA" : String.join(", ", languages);
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase();
        if (description.contains("courant") || description.contains("fluent")) {
            return "Courant";
        } else if (description.contains("intermediaire") || description.contains("moyen")) {
            return "Intermediaire";
        } else if (description.contains("debutant") || description.contains("basic")) {
            return "Debutant";
        }
        return "NA";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("postDescription", "").toLowerCase();
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
