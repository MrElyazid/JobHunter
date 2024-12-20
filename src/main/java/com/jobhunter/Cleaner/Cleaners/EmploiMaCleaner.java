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

public class EmploiMaCleaner implements JobCleaner {
    private JSONArray hardSkills;
    private JSONArray softSkills;
    private JSONArray regions;
    private JSONArray sectors;
    private JSONArray contractTypes;
    private JSONArray diplomaTypes;
    private JSONArray personalityTraits;

    public EmploiMaCleaner() {
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
        cleanedJobOffer.put("company", cleanText(rawJobOffer.optString("company", "NA")));
        cleanedJobOffer.put("foriegn_company", isForeignCompany(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("company_description", cleanText(rawJobOffer.optString("company_description", "NA")));
        cleanedJobOffer.put("contract_type", extractContractType(rawJobOffer));
        cleanedJobOffer.put("is_internship", isInternship(rawJobOffer) ? 1 : 0);
        cleanedJobOffer.put("source", "EmploiMa");
        cleanedJobOffer.put("link", rawJobOffer.optString("link", "NA"));
        cleanedJobOffer.put("min_experience", extractMinExperience(rawJobOffer));
        cleanedJobOffer.put("diploma", new JSONArray(extractDiploma(rawJobOffer)).toString());
        cleanedJobOffer.put("title", cleanText(rawJobOffer.optString("title", "NA")));
        cleanedJobOffer.put("application_date", LocalDate.now().toString());
        cleanedJobOffer.put("date_of_publication", LocalDate.now().toString());
        cleanedJobOffer.put("company_address", cleanText(extractCompanyAddress(rawJobOffer)));
        cleanedJobOffer.put("company_website", extractCompanyWebsite(rawJobOffer));
        cleanedJobOffer.put("region", extractRegion(rawJobOffer));
        cleanedJobOffer.put("desired_profile", cleanText(rawJobOffer.optString("job_qualifications", "NA")));
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
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String ville = criteria.optString("Ville", "");
            if (!ville.isEmpty()) {
                return cleanText(ville);
            }
        }
        return "NA";
    }

    private String extractSector(JSONObject jobOffer) {
        String sector = jobOffer.optString("company_sector", "");
        if (!sector.isEmpty() && !sector.equals("Unknown")) {
            for (int i = 0; i < sectors.length(); i++) {
                if (sector.toLowerCase().contains(sectors.getString(i).toLowerCase())) {
                    return cleanText(sectors.getString(i));
                }
            }
        }
        return "NA";
    }

    private float extractSalary(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String salaryRange = criteria.optString("Salaire proposé", "");
            if (!salaryRange.isEmpty()) {
                Pattern pattern = Pattern.compile("(\\d+)");
                Matcher matcher = pattern.matcher(salaryRange);
                if (matcher.find()) {
                    return Float.parseFloat(matcher.group(1));
                }
            }
        }
        return 0.0f;
    }

    private boolean isRemote(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String remote = criteria.optString("Travail à distance", "Non");
            return remote.equalsIgnoreCase("Oui");
        }
        return false;
    }

    private List<String> extractSkills(JSONObject jobOffer, boolean isHardSkill) {
        List<String> foundSkills = new ArrayList<>();
        JSONArray skills = jobOffer.optJSONArray("skills");
        
        if (skills != null) {
            JSONArray skillsToCheck = isHardSkill ? hardSkills : softSkills;
            for (int i = 0; i < skills.length(); i++) {
                String skill = skills.getString(i);
                for (int j = 0; j < skillsToCheck.length(); j++) {
                    if (skill.toLowerCase().contains(skillsToCheck.getString(j).toLowerCase())) {
                        foundSkills.add(skillsToCheck.getString(j));
                    }
                }
            }
        }
        return foundSkills;
    }

    private boolean isForeignCompany(JSONObject jobOffer) {
        String description = jobOffer.optString("company_description", "").toLowerCase();
        return description.contains("international") || description.contains("multinational") ||
               description.contains("etranger") || description.contains("groupe international");
    }

    private String extractContractType(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String contractType = criteria.optString("Type de contrat", "");
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
        String description = jobOffer.optString("job_description", "").toLowerCase();
        return title.contains("stage") || title.contains("stagiaire") || 
               description.contains("stage") || description.contains("stagiaire");
    }

    private int extractMinExperience(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String experience = criteria.optString("Niveau d'expérience", "");
            Pattern pattern = Pattern.compile("(\\d+)");
            Matcher matcher = pattern.matcher(experience);
            if (matcher.find()) {
                return Integer.parseInt(matcher.group(1));
            }
        }
        return 0;
    }

    private List<String> extractDiploma(JSONObject jobOffer) {
        List<String> foundDiplomas = new ArrayList<>();
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String education = criteria.optString("Niveau d'études", "").toLowerCase();
            for (int i = 0; i < diplomaTypes.length(); i++) {
                if (education.contains(diplomaTypes.getString(i).toLowerCase())) {
                    foundDiplomas.add(diplomaTypes.getString(i));
                }
            }
        }
        return foundDiplomas.isEmpty() ? List.of("NA") : foundDiplomas;
    }

    private String extractCompanyAddress(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String ville = criteria.optString("Ville", "");
            if (!ville.isEmpty()) {
                return cleanText(ville);
            }
        }
        return "NA";
    }

    private String extractCompanyWebsite(JSONObject jobOffer) {
        String website = jobOffer.optString("company_website", "");
        if (!website.isEmpty() && !website.equals("No website")) {
            return website;
        }
        return "NA";
    }

    private String extractRegion(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String region = criteria.optString("Région", "");
            if (!region.isEmpty()) {
                for (int i = 0; i < regions.length(); i++) {
                    if (region.toLowerCase().contains(regions.getString(i).toLowerCase())) {
                        return cleanText(regions.getString(i));
                    }
                }
            }
        }
        return "NA";
    }

    private String extractPersonalityTraits(JSONObject jobOffer) {
        String description = jobOffer.optString("job_description", "") + " " + 
                           jobOffer.optString("job_qualifications", "");
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
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String languages = criteria.optString("Langues exigées", "");
            if (!languages.isEmpty()) {
                List<String> foundLanguages = new ArrayList<>();
                if (languages.toLowerCase().contains("francais")) foundLanguages.add("francais");
                if (languages.toLowerCase().contains("anglais")) foundLanguages.add("anglais");
                if (languages.toLowerCase().contains("arabe")) foundLanguages.add("arabe");
                if (languages.toLowerCase().contains("espagnol")) foundLanguages.add("espagnol");
                if (!foundLanguages.isEmpty()) {
                    return String.join(", ", foundLanguages);
                }
            }
        }
        return "NA";
    }

    private String extractLanguageProficiency(JSONObject jobOffer) {
        JSONObject criteria = jobOffer.optJSONObject("job_criteria");
        if (criteria != null) {
            String languages = criteria.optString("Langues exigées", "").toLowerCase();
            if (languages.contains("courant")) {
                return "Courant";
            } else if (languages.contains("intermediaire")) {
                return "Intermediaire";
            } else if (languages.contains("debutant") || languages.contains("basique")) {
                return "Debutant";
            }
        }
        return "NA";
    }

    private String extractRecommendedSkills(JSONObject jobOffer) {
        List<String> skills = new ArrayList<>();
        JSONArray jobSkills = jobOffer.optJSONArray("skills");
        
        if (jobSkills != null) {
            String[] recommendedSkills = {"git", "agile", "scrum", "docker", "jenkins", "aws", 
                                        "azure", "linux", "windows", "office", "excel"};
            for (String recommendedSkill : recommendedSkills) {
                for (int i = 0; i < jobSkills.length(); i++) {
                    if (jobSkills.getString(i).toLowerCase().contains(recommendedSkill)) {
                        skills.add(recommendedSkill);
                        break;
                    }
                }
            }
        }
        return skills.isEmpty() ? "NA" : String.join(", ", skills);
    }
}
