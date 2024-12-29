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

public class AnapecCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;
    private JSONArray personalityTraits;

    public AnapecCleaner() {
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
        cleanedJobOffer.put("min_salary", extractSalary(rawJobOffer));
        cleanedJobOffer.put("is_remote", isRemote(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("hard_skills", new JSONArray(extractSkills(rawJobOffer, true)).toString());
        cleanedJobOffer.put("soft_skills", new JSONArray(extractSkills(rawJobOffer, false)).toString());
        cleanedJobOffer.put("company", cleanText(extractCompanyName(rawJobOffer)));
        cleanedJobOffer.put("foriegn_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanText(rawJobOffer.optString("company_description", "NA")));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "Anapec");
        cleanedJobOffer.put("link", rawJobOffer.optString("link", "NA"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", new JSONArray(extractDiploma(rawJobOffer)).toString());
        cleanedJobOffer.put("title", cleanText(rawJobOffer.optString("title", "NA")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", extractPublicationDate(rawJobOffer));
        cleanedJobOffer.put("company_address", cleanText(extractCompanyAddress(rawJobOffer)));
        cleanedJobOffer.put("company_website", "NA");
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanText(rawJobOffer.optString("profile_requirements", "NA")));
        cleanedJobOffer.put("personality_traits", extractPersonalityTraits(rawJobOffer));
        cleanedJobOffer.put("languages", extractLanguages(rawJobOffer));
        cleanedJobOffer.put("language_profeciency", extractLanguageProficiency(rawJobOffer));
        cleanedJobOffer.put("recommended_skills", extractRecommendedSkills(rawJobOffer));
        cleanedJobOffer.put("job", cleanText(rawJobOffer.optString("input_title", "NA")));

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
        String companyDesc = jobOffer.optString("company_description", "");
        Pattern pattern = Pattern.compile("Lieu de travail\\s*:\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(companyDesc);
        if (matcher.find()) {
            return cleanText(matcher.group(1));
        }
        return "NA";
    }

    private String extractSector(JSONObject jobOffer) {
        String companyDesc = jobOffer.optString("company_description", "");
        Pattern pattern = Pattern.compile("Secteur d'activité\\s*:\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(companyDesc);
        if (matcher.find()) {
            String sector = matcher.group(1);
            for (int i = 0; i < sectors.length(); i++) {
                if (sector.toLowerCase().contains(sectors.getString(i).toLowerCase())) {
                    return cleanText(sectors.getString(i));
                }
            }
        }
        return "NA";
    }

    private float extractSalary(JSONObject jobOffer) {
        String companyDesc = jobOffer.optString("company_description", "");
        Pattern pattern = Pattern.compile("(\\d+)\\s*(?:dh|dirhams|MAD)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(companyDesc);
        return matcher.find() ? Float.parseFloat(matcher.group(1)) : 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "").toLowerCase() + 
                           jobOffer.optString("job_description", "").toLowerCase();
        return description.contains("teletravail") || description.contains("a distance") || 
               description.contains("remote") || description.contains("travail a domicile");
    }

    private List<String> extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        String description = jobOffer.optString("company_description", "") + " " + 
                           jobOffer.optString("profile_requirements", "") + " " +
                           jobOffer.optString("job_description", "");
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

    private String extractCompanyName(JSONObject jobOffer) {
        String agency = jobOffer.optString("agency", "");
        if (!agency.isEmpty() && !agency.equals("N/A")) {
            return cleanText(agency.split("/")[0]);
        }
        return "NA";
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("etranger") || description.contains("groupe international");
    }

    private String extractContractType(JSONObject jobOffer) {
        String companyDesc = jobOffer.optString("company_description", "");
        Pattern pattern = Pattern.compile("Type de contrat\\s*:\\s*([^\\n]+)");
        Matcher matcher = pattern.matcher(companyDesc);
        if (matcher.find()) {
            String contractType = matcher.group(1);
            for (int i = 0; i < contractTypes.length(); i++) {
                if (contractType.toLowerCase().contains(contractTypes.getString(i).toLowerCase())) {
                    return cleanText(contractTypes.getString(i));
                }
            }
        }
        return "NA";
    }

    private boolean isInternship(JSONObject jobOffer) {
        String title = jobOffer.optString("title", "").toLowerCase();
        String description = jobOffer.optString("company_description", "").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") || 
               description.contains("stage") || description.contains("stagiaire");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        String companyDesc = jobOffer.optString("company_description", "");
        Pattern pattern = Pattern.compile("Expérience\\s*(?:professionnelle)?\\s*:?\\s*\\(?\\s*(\\d+)\\s*(?:ans?|années?)", 
                                        Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(companyDesc);
        return matcher.find() ? Integer.parseInt(matcher.group(1)) : 0;
    }

    private List<String> extractDiploma(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "") + " " +
                           jobOffer.optString("profile_requirements", "");
        List<String> foundDiplomas = new ArrayList<>();
        
        for (int i = 0; i < diplomaTypes.length(); i++) {
            if (description.toLowerCase().contains(diplomaTypes.getString(i).toLowerCase())) {
                foundDiplomas.add(diplomaTypes.getString(i));
            }
        }
        return foundDiplomas.isEmpty() ? List.of("NA") : foundDiplomas;
    }

    private String extractPublicationDate(JSONObject jobOffer) {
        String date = jobOffer.optString("date", "");
        if (!date.isEmpty() && !date.equals("N/A")) {
            try {
                DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                return LocalDate.parse(date, inputFormatter).toString();
            } catch (Exception e) {
                return LocalDate.now().toString();
            }
        }
        return LocalDate.now().toString();
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        String location = extractLocation(jobOffer);
        return location.equals("NA") ? "NA" : cleanText(location);
    }

    private String extractRegion(JSONObject jobOffer) {
        String agency = jobOffer.optString("agency", "");
        if (!agency.isEmpty() && !agency.equals("N/A")) {
            for (int i = 0; i < regions.length(); i++) {
                if (agency.toLowerCase().contains(regions.getString(i).toLowerCase())) {
                    return cleanText(regions.getString(i));
                }
            }
        }
        return "NA";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "") + " " +
                           jobOffer.optString("profile_requirements", "");
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
        String profileReq = jobOffer.optString("profile_requirements", "");
        Pattern pattern = Pattern.compile("Langues\\s*:([^;]+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(profileReq);
        if (matcher.find()) {
            List<String> languages = new ArrayList<>();
            String langSection = matcher.group(1).toLowerCase();
            if (langSection.contains("francais")) languages.add("francais");
            if (langSection.contains("anglais")) languages.add("anglais");
            if (langSection.contains("arabe")) languages.add("arabe");
            if (langSection.contains("espagnol")) languages.add("espagnol");
            return languages.isEmpty() ? "NA" : String.join(", ", languages);
        }
        return "NA";
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        String profileReq = jobOffer.optString("profile_requirements", "").toLowerCase();
        if (profileReq.contains("courant") || profileReq.contains("excellent")) {
            return "Courant";
        } else if (profileReq.contains("bon")) {
            return "Intermediaire";
        } else if (profileReq.contains("moyen") || profileReq.contains("basique")) {
            return "Debutant";
        }
        return "NA";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "") + " " +
                           jobOffer.optString("profile_requirements", "");
        List<String> skills = new ArrayList<>();
        String[] recommendedSkills = {"git", "agile", "scrum", "docker", "jenkins", "aws", 
                                    "azure", "linux", "windows", "office", "excel"};

        for (String skill : recommendedSkills) {
            if (description.toLowerCase().contains(skill)) {
                skills.add(skill);
            }
        }
        return skills.isEmpty() ? "NA" : String.join(", ", skills);
    }
}