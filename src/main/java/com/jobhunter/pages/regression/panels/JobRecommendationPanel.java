package com.jobhunter.pages.regression.panels;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.CSVLoader;
import weka.core.Attribute;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import weka.core.neighboursearch.LinearNNSearch;
import weka.core.EuclideanDistance;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.jobhunter.util.DataPreprocessor;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JobRecommendationPanel extends JPanel {
    // ... [previous code remains the same until line 334] ...
    private JComboBox<String> sectorCombo;
    private JComboBox<String> cityCombo;
    private JSpinner salarySpinner;
    private JSpinner experienceSpinner;
    private JCheckBox remoteCheckbox;
    private JComboBox<String> contractTypeCombo;
    private JTextArea skillsArea;
    private JTextArea resultArea;
    private Instances dataset;
    private LinearNNSearch nearestNeighbor;
    private static final ObjectMapper mapper = new ObjectMapper();
    
    public JobRecommendationPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Form Panel
        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Sector Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Preferred Sector:"), gbc);
        
        gbc.gridx = 1;
        sectorCombo = new JComboBox<>();
        formPanel.add(sectorCombo, gbc);
        
        // City Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Preferred City:"), gbc);
        
        gbc.gridx = 1;
        cityCombo = new JComboBox<>();
        formPanel.add(cityCombo, gbc);
        
        // Desired Salary
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Desired Salary (MAD):"), gbc);
        
        gbc.gridx = 1;
        salarySpinner = new JSpinner(new SpinnerNumberModel(5000, 0, 100000, 1000));
        formPanel.add(salarySpinner, gbc);
        
        // Experience
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Years of Experience:"), gbc);
        
        gbc.gridx = 1;
        experienceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        formPanel.add(experienceSpinner, gbc);
        
        // Remote Work
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Remote Work:"), gbc);
        
        gbc.gridx = 1;
        remoteCheckbox = new JCheckBox("Interested in remote work");
        formPanel.add(remoteCheckbox, gbc);
        
        // Contract Type
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Contract Type:"), gbc);
        
        gbc.gridx = 1;
        contractTypeCombo = new JComboBox<>(new String[]{"Any", "CDI", "CDD", "CI", "STAGE"});
        formPanel.add(contractTypeCombo, gbc);
        
        // Skills
        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Skills (one per line):"), gbc);
        
        gbc.gridx = 1;
        skillsArea = new JTextArea(4, 20);
        JScrollPane skillsScroll = new JScrollPane(skillsArea);
        formPanel.add(skillsScroll, gbc);
        
        // Find Jobs Button
        gbc.gridx = 0;
        gbc.gridy = 7;
        gbc.gridwidth = 2;
        JButton findButton = new JButton("Find Matching Jobs");
        findButton.addActionListener(e -> findMatchingJobs());
        formPanel.add(findButton, gbc);
        
        // Add form to north
        add(formPanel, BorderLayout.NORTH);
        
        // Results Area
        resultArea = new JTextArea(15, 40);
        resultArea.setEditable(false);
        JScrollPane resultScroll = new JScrollPane(resultArea);
        resultScroll.setBorder(BorderFactory.createTitledBorder("Recommended Jobs"));
        add(resultScroll, BorderLayout.CENTER);
        
        // Initialize the dataset
        initializeDataset();
    }
    
    private void initializeDataset() {
        try {
            // Preprocess the CSV data
            Path csvPath = Paths.get("src/main/resources/ML/data.csv");
            Path cleanedPath = DataPreprocessor.preprocessJobData(csvPath.toString());
            
            // Load cleaned CSV data
            CSVLoader loader = new CSVLoader();
            loader.setSource(cleanedPath.toFile());
            dataset = loader.getDataSet();
            
            // Handle missing values
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(dataset);
            dataset = Filter.useFilter(dataset, replaceMissing);
            
            // Convert string attributes to nominal
            StringToNominal stringToNominal = new StringToNominal();
            stringToNominal.setAttributeRange("first-last");
            stringToNominal.setInputFormat(dataset);
            dataset = Filter.useFilter(dataset, stringToNominal);
            
            // Remove instances with missing or invalid values
            Instances filteredData = new Instances(dataset, dataset.numInstances());
            Set<String> sectors = new HashSet<>();
            Set<String> cities = new HashSet<>();
            
            for (int i = 0; i < dataset.numInstances(); i++) {
                Instance inst = dataset.instance(i);
                boolean isValid = true;
                
                // Check required numeric fields
                if (inst.isMissing(dataset.attribute("min_salary")) || 
                    inst.value(dataset.attribute("min_salary")) <= 0) {
                    isValid = false;
                }
                
                // Add valid instance
                if (isValid) {
                    filteredData.add(inst);
                    
                    String sector = inst.stringValue(dataset.attribute("sector"));
                    String city = inst.stringValue(dataset.attribute("location"));
                    if (!sector.isEmpty() && !sector.equals("?")) sectors.add(sector);
                    if (!city.isEmpty() && !city.equals("?")) cities.add(city);
                }
            }
            
            if (filteredData.numInstances() == 0) {
                throw new Exception("No valid instances found after filtering");
            }
            
            // Update dataset with filtered data
            dataset = filteredData;
            
            // Setup nearest neighbor search
            nearestNeighbor = new LinearNNSearch(dataset);
            EuclideanDistance distance = new EuclideanDistance(dataset);
            distance.setDontNormalize(true);
            nearestNeighbor.setDistanceFunction(distance);
            
            // Populate comboboxes
            ArrayList<String> sortedSectors = new ArrayList<>(sectors);
            ArrayList<String> sortedCities = new ArrayList<>(cities);
            sortedSectors.sort(String::compareTo);
            sortedCities.sort(String::compareTo);
            
            for (String sector : sortedSectors) sectorCombo.addItem(sector);
            for (String city : sortedCities) cityCombo.addItem(city);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error initializing dataset: " + e.getMessage() + "\nPlease check the CSV file format.",
                "Initialization Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void findMatchingJobs() {
        try {
            if (nearestNeighbor == null || dataset == null) {
                throw new Exception("Dataset not properly initialized");
            }
            
            // Create a query instance with user preferences
            Instance queryInstance = new weka.core.DenseInstance(dataset.numAttributes());
            queryInstance.setDataset(dataset);
            
            // Set attribute values based on user input
            queryInstance.setValue(dataset.attribute("sector"), sectorCombo.getSelectedItem().toString());
            queryInstance.setValue(dataset.attribute("location"), cityCombo.getSelectedItem().toString());
            queryInstance.setValue(dataset.attribute("min_salary"), (Integer)salarySpinner.getValue());
            queryInstance.setValue(dataset.attribute("min_experience"), (Integer)experienceSpinner.getValue());
            queryInstance.setValue(dataset.attribute("is_remote"), remoteCheckbox.isSelected() ? "true" : "false");
            
            String contractType = contractTypeCombo.getSelectedItem().toString();
            if (!contractType.equals("Any")) {
                queryInstance.setValue(dataset.attribute("contract_type"), contractType);
            }
            
            // Get user skills
            Set<String> userSkills = new HashSet<>(Arrays.asList(
                skillsArea.getText().toLowerCase().split("\n")));
            
            // Find nearest neighbors
            Instances neighbors = nearestNeighbor.kNearestNeighbours(queryInstance, 20);
            
            // Score and rank jobs
            ArrayList<JobMatch> matches = new ArrayList<>();
            for (int i = 0; i < neighbors.numInstances(); i++) {
                Instance job = neighbors.instance(i);
                
                // Calculate match score based on various factors
                double score = calculateMatchScore(job, queryInstance, userSkills);
                
                matches.add(new JobMatch(
                    job.stringValue(dataset.attribute("title")),
                    job.stringValue(dataset.attribute("company")),
                    job.stringValue(dataset.attribute("sector")),
                    job.stringValue(dataset.attribute("location")),
                    job.value(dataset.attribute("min_salary")),
                    score
                ));
            }
            
            // Sort by match score
            matches.sort((a, b) -> Double.compare(b.score, a.score));
            
            // Display results
            StringBuilder result = new StringBuilder();
            result.append("Top Matching Jobs:\n\n");
            
            int displayCount = 0;
            for (JobMatch match : matches) {
                if (match.score >= 0.4 && displayCount < 10) { // Show only good matches
                    result.append(String.format("%d. %s\n", displayCount + 1, match.title));
                    result.append(String.format("   Company: %s\n", match.company));
                    result.append(String.format("   Location: %s\n", match.location));
                    result.append(String.format("   Sector: %s\n", match.sector));
                    result.append(String.format("   Salary: %.0f MAD\n", match.salary));
                    result.append(String.format("   Match Score: %.1f%%\n\n", match.score * 100));
                    displayCount++;
                }
            }
            
            if (displayCount == 0) {
                result.append("No jobs found matching your criteria.\n");
                result.append("Try adjusting your preferences or adding more skills.");
            }
            
            resultArea.setText(result.toString());
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error finding matches: " + e.getMessage(),
                "Search Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private double calculateMatchScore(Instance job, Instance query, Set<String> userSkills) {
        double score = 0.0;
        
        try {
            // Sector match (30%)
            if (job.stringValue(dataset.attribute("sector")).equals(
                query.stringValue(dataset.attribute("sector")))) {
                score += 0.3;
            }
            
            // Location match (20%)
            if (job.stringValue(dataset.attribute("location")).equals(
                query.stringValue(dataset.attribute("location")))) {
                score += 0.2;
            }
            
            // Salary match (20%) - within 20% range
            double querySalary = query.value(dataset.attribute("min_salary"));
            double jobSalary = job.value(dataset.attribute("min_salary"));
            if (Math.abs(jobSalary - querySalary) <= querySalary * 0.2) {
                score += 0.2;
            }
            
            // Experience match (15%) - within 2 years
            double queryExp = query.value(dataset.attribute("min_experience"));
            double jobExp = job.value(dataset.attribute("min_experience"));
            if (Math.abs(jobExp - queryExp) <= 2) {
                score += 0.15;
            }
            
            // Remote work match (15%)
            if (job.stringValue(dataset.attribute("is_remote")).equals(
                query.stringValue(dataset.attribute("is_remote")))) {
                score += 0.15;
            }
            
            // Skills match (20% bonus)
            try {
                String skillsStr = job.stringValue(dataset.attribute("hard_skills"));
                if (!skillsStr.equals("?") && !skillsStr.equals("[]")) {
                    @SuppressWarnings("unchecked")
                    java.util.List<String> jobSkills = mapper.readValue(skillsStr, ArrayList.class);
                    
                    int matchingSkills = 0;
                    for (String skill : jobSkills) {
                        if (userSkills.contains(skill.trim().toLowerCase())) {
                            matchingSkills++;
                        }
                    }
                    
                    if (jobSkills.size() > 0) {
                        score += (matchingSkills / (double)jobSkills.size()) * 0.2;
                    }
                }
            } catch (Exception e) {
                // Skills parsing failed, ignore skills score
            }
            
        } catch (Exception e) {
            // Error calculating score, return current score
        }
        
        return score;
    }
    
    private static class JobMatch {
        String title;
        String company;
        String sector;
        String location;
        double salary;
        double score;
        
        JobMatch(String title, String company, String sector, String location, 
                double salary, double score) {
            this.title = title;
            this.company = company;
            this.sector = sector;
            this.location = location;
            this.salary = salary;
            this.score = score;
        }
    }
}
