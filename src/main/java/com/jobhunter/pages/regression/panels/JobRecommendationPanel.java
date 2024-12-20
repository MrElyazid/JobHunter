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
public class JobRecommendationPanel extends JPanel {
    private JComboBox<String> sectorCombo;
    private JComboBox<String> cityCombo;
    private JSpinner salarySpinner;
    private JSpinner experienceSpinner;
    private JCheckBox remoteCheckbox;
    private JComboBox<String> contractTypeCombo;
    private JTextArea resultArea;
    private Instances dataset;
    private LinearNNSearch nearestNeighbor;
    
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
        
        // Find Jobs Button
        gbc.gridx = 0;
        gbc.gridy = 6;
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
            
            // Create attributes list
            ArrayList<Attribute> attributes = new ArrayList<>();
            
            // First collect all unique values for nominal attributes
            Set<String> sectors = new HashSet<>();
            Set<String> cities = new HashSet<>();
            Set<String> contractTypes = new HashSet<>();
            
            // Iterate through original dataset to collect unique values
            for (int i = 0; i < dataset.numInstances(); i++) {
                Instance inst = dataset.instance(i);
                Attribute sectorAttr = dataset.attribute("sector");
                Attribute locationAttr = dataset.attribute("location");
                Attribute contractAttr = dataset.attribute("contract_type");
                
                if (sectorAttr != null) {
                    String sector = inst.toString(sectorAttr);
                    if (!sector.isEmpty() && !sector.equals("?")) sectors.add(sector);
                }
                
                if (locationAttr != null) {
                    String city = inst.toString(locationAttr);
                    if (!city.isEmpty() && !city.equals("?")) cities.add(city);
                }
                
                if (contractAttr != null) {
                    String contract = inst.toString(contractAttr);
                    if (!contract.isEmpty() && !contract.equals("?")) contractTypes.add(contract);
                }
            }
            
            // Define attributes in specific order
            // Numeric attributes
            attributes.add(new Attribute("min_salary"));
            attributes.add(new Attribute("min_experience"));
            
            // Nominal attributes
            ArrayList<String> sectorList = new ArrayList<>(sectors);
            ArrayList<String> cityList = new ArrayList<>(cities);
            ArrayList<String> contractList = new ArrayList<>(contractTypes);
            ArrayList<String> remoteList = new ArrayList<>(Arrays.asList("0", "1"));
            
            sectorList.sort(String::compareTo);
            cityList.sort(String::compareTo);
            contractList.sort(String::compareTo);
            
            attributes.add(new Attribute("sector", sectorList));
            attributes.add(new Attribute("location", cityList));
            attributes.add(new Attribute("contract_type", contractList));
            attributes.add(new Attribute("is_remote", remoteList));
            
            // String attributes for display purposes
            attributes.add(new Attribute("title", (ArrayList<String>)null));
            attributes.add(new Attribute("company", (ArrayList<String>)null));
            attributes.add(new Attribute("job_description", (ArrayList<String>)null));
            
            // Create new dataset
            Instances newDataset = new Instances("jobs", attributes, dataset.numInstances());
            
            // Set numeric attributes as default class
            newDataset.setClassIndex(0);  // min_salary as default class
            
            // Copy data to new dataset
            for (int i = 0; i < dataset.numInstances(); i++) {
                Instance oldInst = dataset.instance(i);
                double[] values = new double[attributes.size()];
                
                // Set numeric values
                values[0] = oldInst.value(dataset.attribute("min_salary"));
                values[1] = oldInst.value(dataset.attribute("min_experience"));
                
                // Set nominal values
                Attribute sectorAttr = newDataset.attribute("sector");
                Attribute locationAttr = newDataset.attribute("location");
                Attribute contractAttr = newDataset.attribute("contract_type");
                Attribute remoteAttr = newDataset.attribute("is_remote");
                
                String sectorVal = oldInst.toString(dataset.attribute("sector"));
                String locationVal = oldInst.toString(dataset.attribute("location"));
                String contractVal = oldInst.toString(dataset.attribute("contract_type"));
                String remoteVal = oldInst.toString(dataset.attribute("is_remote"));
                
                values[2] = sectorAttr.indexOfValue(sectorVal);
                values[3] = locationAttr.indexOfValue(locationVal);
                values[4] = contractAttr.indexOfValue(contractVal);
                values[5] = remoteAttr.indexOfValue(remoteVal);
                
                // Set string values
                values[6] = newDataset.attribute("title").addStringValue(oldInst.toString(dataset.attribute("title")));
                values[7] = newDataset.attribute("company").addStringValue(oldInst.toString(dataset.attribute("company")));
                values[8] = newDataset.attribute("job_description").addStringValue(oldInst.toString(dataset.attribute("job_description")));
                
                Instance newInst = new weka.core.DenseInstance(1.0, values);
                newInst.setDataset(newDataset);
                newDataset.add(newInst);
            }
            
            // Update dataset reference
            dataset = newDataset;
            
            // Remove instances with missing or invalid values
            Instances filteredData = new Instances(dataset, dataset.numInstances());
            
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
            
            // Create query instance with known structure
            double[] queryValues = new double[dataset.numAttributes()];
            
            // Set numeric values (indices 0 and 1)
            queryValues[0] = ((Number)salarySpinner.getValue()).doubleValue();
            queryValues[1] = ((Number)experienceSpinner.getValue()).doubleValue();
            
            // Set nominal values (indices 2-5)
            String sectorValue = sectorCombo.getSelectedItem().toString();
            String locationValue = cityCombo.getSelectedItem().toString();
            String contractValue = contractTypeCombo.getSelectedItem().toString();
            String remoteValue = remoteCheckbox.isSelected() ? "1" : "0";
            
            queryValues[2] = dataset.attribute("sector").indexOfValue(sectorValue);
            queryValues[3] = dataset.attribute("location").indexOfValue(locationValue);
            queryValues[4] = !contractValue.equals("Any") ? 
                dataset.attribute("contract_type").indexOfValue(contractValue.toUpperCase().trim()) :
                weka.core.Utils.missingValue();
            queryValues[5] = dataset.attribute("is_remote").indexOfValue(remoteValue);
            
            // Set string attributes as missing (indices 6-8)
            queryValues[6] = weka.core.Utils.missingValue();
            queryValues[7] = weka.core.Utils.missingValue();
            queryValues[8] = weka.core.Utils.missingValue();
            
            Instance queryInstance = new weka.core.DenseInstance(1.0, queryValues);
            queryInstance.setDataset(dataset);
            
            // Find nearest neighbors
            Instances neighbors = nearestNeighbor.kNearestNeighbours(queryInstance, 10);
            
            // Score and rank jobs
            ArrayList<JobMatch> matches = new ArrayList<>();
            for (int i = 0; i < neighbors.numInstances(); i++) {
                Instance job = neighbors.instance(i);
                
                // Calculate match score based on various factors
                double score = calculateMatchScore(job, queryInstance);
                
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
                result.append("Try adjusting your preferences.");
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
    
    private double calculateMatchScore(Instance job, Instance query) {
        double score = 0.0;
        
        try {
            // Salary match (20%) - within 20% range
            double querySalary = query.value(0); // min_salary is at index 0
            double jobSalary = job.value(0);
            if (Math.abs(jobSalary - querySalary) <= querySalary * 0.2) {
                score += 0.2;
            }
            
            // Experience match (15%) - within 2 years
            double queryExp = query.value(1); // min_experience is at index 1
            double jobExp = job.value(1);
            if (Math.abs(jobExp - queryExp) <= 2) {
                score += 0.15;
            }
            
            // Sector match (30%)
            if (job.value(2) == query.value(2)) { // sector is at index 2
                score += 0.3;
            }
            
            // Location match (20%)
            if (job.value(3) == query.value(3)) { // location is at index 3
                score += 0.2;
            }
            
            // Contract type match (bonus 10% if specified and matches)
            if (!query.isMissing(4) && job.value(4) == query.value(4)) { // contract_type is at index 4
                score += 0.1;
            }
            
            // Remote work match (15%)
            if (job.value(5) == query.value(5)) { // is_remote is at index 5
                score += 0.15;
            }
            
        } catch (Exception e) {
            e.printStackTrace();
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
