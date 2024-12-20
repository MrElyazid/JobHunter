package com.jobhunter.pages.regression.panels;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
import weka.classifiers.functions.LinearRegression;
import weka.core.Attribute;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.jobhunter.util.DataPreprocessor;

public class SalaryPredictionPanel extends JPanel {
    private JComboBox<String> sectorCombo;
    private JComboBox<String> cityCombo;
    private JSpinner experienceSpinner;
    private JLabel resultLabel;
    private LinearRegression model;
    private Instances trainingData;
    
    public SalaryPredictionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Sector Selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Sector:"), gbc);
        
        gbc.gridx = 1;
        sectorCombo = new JComboBox<>();
        inputPanel.add(sectorCombo, gbc);
        
        // City Selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("City:"), gbc);
        
        gbc.gridx = 1;
        cityCombo = new JComboBox<>();
        inputPanel.add(cityCombo, gbc);
        
        // Experience Input
        gbc.gridx = 0;
        gbc.gridy = 2;
        inputPanel.add(new JLabel("Experience (years):"), gbc);
        
        gbc.gridx = 1;
        experienceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        inputPanel.add(experienceSpinner, gbc);
        
        // Predict Button
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        JButton predictButton = new JButton("Predict Salary");
        predictButton.addActionListener(e -> predictSalary());
        inputPanel.add(predictButton, gbc);
        
        // Result Label
        gbc.gridy = 4;
        resultLabel = new JLabel("Predicted salary will appear here");
        resultLabel.setHorizontalAlignment(SwingConstants.CENTER);
        inputPanel.add(resultLabel, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Model Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Model Information"));
        JTextArea infoText = new JTextArea(5, 40);
        infoText.setEditable(false);
        infoText.setText("This model predicts salary based on:\n" +
                        "- Sector\n" +
                        "- City\n" +
                        "- Years of experience\n\n" +
                        "Using Linear Regression trained on historical job posting data.");
        infoPanel.add(new JScrollPane(infoText));
        
        add(infoPanel, BorderLayout.CENTER);
        
        // Initialize the model
        initializeModel();
    }
    
    private void initializeModel() {
        try {
            // Preprocess the CSV data
            Path csvPath = Paths.get("src/main/resources/ML/data.csv");
            Path cleanedPath = DataPreprocessor.preprocessJobData(csvPath.toString());
            
            // Load cleaned CSV data
            CSVLoader loader = new CSVLoader();
            loader.setSource(cleanedPath.toFile());
            Instances data = loader.getDataSet();
            
            // Handle missing values
            ReplaceMissingValues replaceMissing = new ReplaceMissingValues();
            replaceMissing.setInputFormat(data);
            data = Filter.useFilter(data, replaceMissing);
            
            // Convert string attributes to nominal
            StringToNominal stringToNominal = new StringToNominal();
            stringToNominal.setAttributeRange("first-last");
            stringToNominal.setInputFormat(data);
            data = Filter.useFilter(data, stringToNominal);
            
            // Keep only required attributes
            ArrayList<Integer> indicesToKeep = new ArrayList<>();
            for (int i = 0; i < data.numAttributes(); i++) {
                Attribute att = data.attribute(i);
                String name = att.name().toLowerCase();
                if (name.equals("sector") || 
                    name.equals("location") || 
                    name.equals("min_salary") || 
                    name.equals("min_experience")) {
                    indicesToKeep.add(i);
                }
            }
            
            if (indicesToKeep.size() < 4) {
                throw new Exception("Required attributes not found in dataset");
            }
            
            Remove remove = new Remove();
            int[] indices = indicesToKeep.stream().mapToInt(i -> i).toArray();
            remove.setAttributeIndicesArray(indices);
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            data = Filter.useFilter(data, remove);
            
            // Convert min_salary to numeric
            Attribute salaryAtt = data.attribute("min_salary");
            if (!salaryAtt.isNumeric()) {
                throw new Exception("min_salary attribute must be numeric");
            }
            
            // Set class attribute to min_salary
            data.setClassIndex(salaryAtt.index());
            
            // Remove instances with invalid salary values
            Instances filteredData = new Instances(data, data.numInstances());
            Set<String> sectors = new HashSet<>();
            Set<String> cities = new HashSet<>();
            
            for (int i = 0; i < data.numInstances(); i++) {
                if (!data.instance(i).isMissing(salaryAtt) && 
                    data.instance(i).value(salaryAtt) > 0) {
                    filteredData.add(data.instance(i));
                    
                    String sector = data.instance(i).stringValue(data.attribute("sector"));
                    String city = data.instance(i).stringValue(data.attribute("location"));
                    if (!sector.isEmpty() && !sector.equals("?")) sectors.add(sector);
                    if (!city.isEmpty() && !city.equals("?")) cities.add(city);
                }
            }
            
            if (filteredData.numInstances() == 0) {
                throw new Exception("No valid instances found after filtering");
            }
            
            // Store filtered data as training data
            trainingData = filteredData;
            
            // Build the model
            model = new LinearRegression();
            model.buildClassifier(trainingData);
            
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
                "Error initializing model: " + e.getMessage() + "\nPlease check the CSV file format.",
                "Model Initialization Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void predictSalary() {
        try {
            if (model == null || trainingData == null) {
                throw new Exception("Model not properly initialized");
            }
            
            // Create a new instance with the selected values
            Instances testInstance = new Instances(trainingData, 0);
            double[] vals = new double[testInstance.numAttributes()];
            
            // Set attribute values
            Attribute sectorAttr = testInstance.attribute("sector");
            Attribute locationAttr = testInstance.attribute("location");
            Attribute expAttr = testInstance.attribute("min_experience");
            
            if (sectorAttr != null && locationAttr != null && expAttr != null) {
                vals[sectorAttr.index()] = sectorAttr.indexOfValue(sectorCombo.getSelectedItem().toString());
                vals[locationAttr.index()] = locationAttr.indexOfValue(cityCombo.getSelectedItem().toString());
                vals[expAttr.index()] = (Integer)experienceSpinner.getValue();
                
                testInstance.add(new weka.core.DenseInstance(1.0, vals));
                testInstance.setClassIndex(testInstance.attribute("min_salary").index());
                
                // Make prediction
                double prediction = model.classifyInstance(testInstance.instance(0));
                
                // Update result label
                if (prediction > 0) {
                    resultLabel.setText(String.format("Predicted Salary: %.2f MAD", prediction));
                } else {
                    resultLabel.setText("Unable to make a reliable prediction with given inputs");
                }
            } else {
                throw new Exception("Required attributes not found in the model");
            }
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error making prediction: " + e.getMessage(),
                "Prediction Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
