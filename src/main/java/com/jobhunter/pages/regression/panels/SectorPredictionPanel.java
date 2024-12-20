package com.jobhunter.pages.regression.panels;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.converters.CSVLoader;
import weka.classifiers.trees.J48;
import weka.core.Attribute;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.StringToNominal;
import weka.filters.unsupervised.attribute.Remove;
import weka.filters.unsupervised.attribute.ReplaceMissingValues;
import java.util.ArrayList;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.jobhunter.util.DataPreprocessor;

public class SectorPredictionPanel extends JPanel {
    private JSpinner salarySpinner;
    private JSpinner experienceSpinner;
    private JTextArea resultArea;
    private J48 model;
    private Instances trainingData;
    
    public SectorPredictionPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Input Panel
        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Desired Salary Input
        gbc.gridx = 0;
        gbc.gridy = 0;
        inputPanel.add(new JLabel("Desired Salary (MAD):"), gbc);
        
        gbc.gridx = 1;
        salarySpinner = new JSpinner(new SpinnerNumberModel(5000, 0, 100000, 1000));
        inputPanel.add(salarySpinner, gbc);
        
        // Experience Input
        gbc.gridx = 0;
        gbc.gridy = 1;
        inputPanel.add(new JLabel("Experience (years):"), gbc);
        
        gbc.gridx = 1;
        experienceSpinner = new JSpinner(new SpinnerNumberModel(0, 0, 30, 1));
        inputPanel.add(experienceSpinner, gbc);
        
        // Predict Button
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JButton predictButton = new JButton("Predict Suitable Sectors");
        predictButton.addActionListener(e -> predictSectors());
        inputPanel.add(predictButton, gbc);
        
        add(inputPanel, BorderLayout.NORTH);
        
        // Results Area
        resultArea = new JTextArea(10, 40);
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Predicted Suitable Sectors"));
        add(scrollPane, BorderLayout.CENTER);
        
        // Model Info Panel
        JPanel infoPanel = new JPanel();
        infoPanel.setLayout(new BoxLayout(infoPanel, BoxLayout.Y_AXIS));
        infoPanel.setBorder(BorderFactory.createTitledBorder("Model Information"));
        JTextArea infoText = new JTextArea(5, 40);
        infoText.setEditable(false);
        infoText.setText("This model predicts suitable sectors based on:\n" +
                        "- Desired salary\n" +
                        "- Years of experience\n\n" +
                        "Using Decision Tree (J48) trained on historical job posting data.\n" +
                        "Results are ranked by confidence score.");
        infoPanel.add(new JScrollPane(infoText));
        
        add(infoPanel, BorderLayout.SOUTH);
        
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
                    name.equals("min_salary") || 
                    name.equals("min_experience")) {
                    indicesToKeep.add(i);
                }
            }
            
            if (indicesToKeep.size() < 3) {
                throw new Exception("Required attributes not found in dataset");
            }
            
            Remove remove = new Remove();
            int[] indices = indicesToKeep.stream().mapToInt(i -> i).toArray();
            remove.setAttributeIndicesArray(indices);
            remove.setInvertSelection(true);
            remove.setInputFormat(data);
            data = Filter.useFilter(data, remove);
            
            // Set class attribute to sector
            data.setClassIndex(data.attribute("sector").index());
            
            // Remove instances with invalid values
            Instances filteredData = new Instances(data, data.numInstances());
            for (int i = 0; i < data.numInstances(); i++) {
                Instance inst = data.instance(i);
                if (!inst.isMissing(data.classIndex()) && 
                    !inst.isMissing(data.attribute("min_salary").index()) &&
                    !inst.isMissing(data.attribute("min_experience").index()) &&
                    inst.value(data.attribute("min_salary").index()) > 0) {
                    filteredData.add(inst);
                }
            }
            
            if (filteredData.numInstances() == 0) {
                throw new Exception("No valid instances found after filtering");
            }
            
            // Store filtered data as training data
            trainingData = filteredData;
            
            // Build the model
            model = new J48();
            model.buildClassifier(trainingData);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                "Error initializing model: " + e.getMessage() + "\nPlease check the CSV file format.",
                "Model Initialization Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void predictSectors() {
        try {
            if (model == null || trainingData == null) {
                throw new Exception("Model not properly initialized");
            }
            
            // Create a new instance with the input values
            Instances testInstance = new Instances(trainingData, 0);
            double[] vals = new double[testInstance.numAttributes()];
            
            // Set attribute values
            Attribute salaryAttr = testInstance.attribute("min_salary");
            Attribute expAttr = testInstance.attribute("min_experience");
            
            if (salaryAttr != null && expAttr != null) {
                vals[salaryAttr.index()] = (Integer)salarySpinner.getValue();
                vals[expAttr.index()] = (Integer)experienceSpinner.getValue();
                
                testInstance.add(new weka.core.DenseInstance(1.0, vals));
                testInstance.setClassIndex(testInstance.attribute("sector").index());
                
                // Get probability distribution for all sectors
                double[] distribution = model.distributionForInstance(testInstance.instance(0));
                
                // Sort sectors by probability
                ArrayList<SectorProbability> sectorProbs = new ArrayList<>();
                for (int i = 0; i < distribution.length; i++) {
                    String sector = testInstance.attribute("sector").value(i);
                    double prob = distribution[i];
                    sectorProbs.add(new SectorProbability(sector, prob));
                }
                sectorProbs.sort((a, b) -> Double.compare(b.probability, a.probability));
                
                // Display top 5 sectors with probabilities
                StringBuilder result = new StringBuilder();
                result.append("Top recommended sectors based on your criteria:\n\n");
                
                int count = 0;
                for (SectorProbability sp : sectorProbs) {
                    if (sp.probability > 0.05 && count < 5) { // Show only sectors with >5% probability
                        result.append(String.format("%d. %s (%.1f%% match)\n", 
                            count + 1, sp.sector, sp.probability * 100));
                        count++;
                    }
                }
                
                if (count == 0) {
                    result.append("No sectors found matching your criteria.\n");
                    result.append("Try adjusting your salary or experience requirements.");
                }
                
                resultArea.setText(result.toString());
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
    
    private static class SectorProbability {
        String sector;
        double probability;
        
        SectorProbability(String sector, double probability) {
            this.sector = sector;
            this.probability = probability;
        }
    }
}
