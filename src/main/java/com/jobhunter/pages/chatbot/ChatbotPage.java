package com.jobhunter.pages.chatbot;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import com.jobhunter.util.DatabaseConnection;
import com.jobhunter.pages.main.MainPage;

public class ChatbotPage {
    private JFrame frame;
    private JTextArea chatArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton backButton;

    public ChatbotPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("JobHunter Chatbot");
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout(10, 10));

        // Top Panel with Back Button and Title
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        backButton = new JButton("← Back");
        backButton.addActionListener(e -> {
            frame.dispose();
            MainPage.getInstance().show();
        });
        topPanel.add(backButton);
        
        JLabel titleLabel = new JLabel("JobHunter Assistant", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(Box.createHorizontalStrut(250));
        topPanel.add(titleLabel);
        frame.add(topPanel, BorderLayout.NORTH);

        // Chat Area
        chatArea = new JTextArea();
        chatArea.setEditable(false);
        chatArea.setWrapStyleWord(true);
        chatArea.setLineWrap(true);
        chatArea.setFont(new Font("Arial", Font.PLAIN, 14));
        chatArea.setMargin(new Insets(10, 10, 10, 10));
        
        JScrollPane scrollPane = new JScrollPane(chatArea);
        frame.add(scrollPane, BorderLayout.CENTER);

        // Input Panel
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        inputField = new JTextField();
        inputField.setFont(new Font("Arial", Font.PLAIN, 14));
        inputField.addActionListener(e -> sendMessage());

        sendButton = new JButton("Send");
        sendButton.addActionListener(e -> sendMessage());

        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        frame.add(inputPanel, BorderLayout.SOUTH);

        // Add initial greeting
        appendMessage("Assistant", "Hello! I'm your JobHunter assistant. I can help you with:\n" +
                     "- Finding jobs by skills or location\n" +
                     "- Salary information for different positions\n" +
                     "- Company information\n" +
                     "- Job market trends\n\n" +
                     "What would you like to know?");
    }

    private void sendMessage() {
        String userMessage = inputField.getText().trim();
        if (userMessage.isEmpty()) return;

        // Display user message
        appendMessage("You", userMessage);
        inputField.setText("");

        // Process the message and generate response
        String response = processUserMessage(userMessage.toLowerCase());
        appendMessage("Assistant", response);
    }

    private String processUserMessage(String message) {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Jobs by location
            if (message.contains("jobs in") || message.contains("jobs at")) {
                String location = extractLocation(message);
                return getJobsByLocation(conn, location);
            }
            
            // Salary information
            if (message.contains("salary") || message.contains("pay")) {
                return getSalaryInformation(conn, message);
            }
            
            // Skills demand
            if (message.contains("skills") || message.contains("requirements")) {
                return getSkillsInformation(conn, message);
            }
            
            // Company information
            if (message.contains("company") || message.contains("companies")) {
                return getCompanyInformation(conn, message);
            }
            
            // Default response
            return "I can help you find information about jobs, salaries, skills, and companies. " +
                   "Try asking something like:\n" +
                   "- What jobs are available in Casablanca?\n" +
                   "- What's the average salary for software engineers?\n" +
                   "- What skills are most in demand?\n" +
                   "- Tell me about companies hiring remotely";
        } catch (SQLException e) {
            e.printStackTrace();
            return "Sorry, I encountered an error while accessing the database. Please try again.";
        }
    }

    private String extractLocation(String message) {
        String[] words = message.split(" ");
        for (int i = 0; i < words.length; i++) {
            if (words[i].equals("in") || words[i].equals("at")) {
                if (i + 1 < words.length) {
                    return words[i + 1].substring(0, 1).toUpperCase() + 
                           words[i + 1].substring(1).toLowerCase();
                }
            }
        }
        return "";
    }

    private String getJobsByLocation(Connection conn, String location) throws SQLException {
        if (location.isEmpty()) {
            return "Please specify a location, e.g., 'Show me jobs in Casablanca'";
        }

        String query = "SELECT COUNT(*) as count, " +
                      "GROUP_CONCAT(DISTINCT sector) as sectors " +
                      "FROM job_post WHERE location LIKE ?";
        
        try (PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, "%" + location + "%");
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt("count");
                String sectors = rs.getString("sectors");
                
                if (count == 0) {
                    return "Sorry, I couldn't find any jobs in " + location;
                }

                return String.format("I found %d jobs in %s.\nMain sectors: %s\n\n" +
                                   "Would you like to know more about specific sectors or salary ranges?",
                                   count, location, sectors);
            }
        }
        return "Sorry, I couldn't find any information for " + location;
    }

    private String getSalaryInformation(Connection conn, String message) throws SQLException {
        String query = "SELECT " +
                      "AVG(min_salary) as avg_salary, " +
                      "MAX(min_salary) as max_salary, " +
                      "COUNT(*) as count " +
                      "FROM job_post WHERE min_salary > 0";

        // Add filters based on message content
        if (message.contains("software") || message.contains("developer")) {
            query += " AND (sector LIKE '%IT%' OR sector LIKE '%Software%')";
        } else if (message.contains("marketing")) {
            query += " AND sector LIKE '%Marketing%'";
        } else if (message.contains("finance")) {
            query += " AND sector LIKE '%Finance%'";
        }

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            if (rs.next()) {
                double avgSalary = rs.getDouble("avg_salary");
                double maxSalary = rs.getDouble("max_salary");
                int count = rs.getInt("count");

                if (count == 0) {
                    return "Sorry, I don't have enough salary data for this query.";
                }

                return String.format("Based on %d job posts:\n" +
                                   "Average salary: %.2f MAD\n" +
                                   "Highest salary: %.2f MAD\n\n" +
                                   "Would you like to know about required skills for these positions?",
                                   count, avgSalary, maxSalary);
            }
        }
        return "Sorry, I couldn't retrieve salary information at the moment.";
    }

    private String getSkillsInformation(Connection conn, String message) throws SQLException {
        // Query to get most common skills from the hard_skills JSON array
        String query = "SELECT hard_skills, COUNT(*) as job_count " +
                      "FROM job_post " +
                      "WHERE hard_skills IS NOT NULL " +
                      "GROUP BY hard_skills " +
                      "ORDER BY job_count DESC " +
                      "LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            StringBuilder response = new StringBuilder("Most in-demand skills:\n");
            while (rs.next()) {
                String skills = rs.getString("hard_skills");
                int count = rs.getInt("job_count");
                response.append(String.format("- %s (%d jobs)\n", skills, count));
            }
            
            response.append("\nWould you like to see jobs requiring specific skills?");
            return response.toString();
        }
    }

    private String getCompanyInformation(Connection conn, String message) throws SQLException {
        String query = "SELECT company, COUNT(*) as job_count, " +
                      "GROUP_CONCAT(DISTINCT sector) as sectors " +
                      "FROM job_post " +
                      "WHERE company IS NOT NULL " +
                      "GROUP BY company " +
                      "ORDER BY job_count DESC " +
                      "LIMIT 5";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            
            StringBuilder response = new StringBuilder("Top hiring companies:\n");
            while (rs.next()) {
                String company = rs.getString("company");
                int count = rs.getInt("job_count");
                String sectors = rs.getString("sectors");
                response.append(String.format("- %s (%d jobs) in %s\n", 
                                           company, count, sectors));
            }
            
            response.append("\nWould you like to know more about specific companies?");
            return response.toString();
        }
    }

    private void appendMessage(String sender, String message) {
        chatArea.append(sender + ": " + message + "\n\n");
        chatArea.setCaretPosition(chatArea.getDocument().getLength());
    }

    public void show() {
        frame.setVisible(true);
    }
}
