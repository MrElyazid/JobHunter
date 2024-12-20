package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;

public class JobDetailsPanel extends JPanel {
    private JEditorPane detailsPane;
    private static final String CSS = 
        "<style>\n" +
        "    body { font-family: Arial, sans-serif; margin: 10px; line-height: 1.4; }\n" +
        "    h2 { color: #2c3e50; margin-top: 10px; margin-bottom: 5px; font-size: 14px; }\n" +
        "    .section { margin-bottom: 8px; }\n" +
        "    .label { color: #7f8c8d; font-weight: bold; }\n" +
        "    .value { color: #2c3e50; }\n" +
        "    .skills { margin: 5px 0 5px 20px; }\n" +
        "    .link { color: #3498db; text-decoration: underline; }\n" +
        "    .date { color: #e74c3c; }\n" +
        "    .highlight { background-color: #f1c40f; padding: 2px 5px; border-radius: 3px; }\n" +
        "    .bullet-point { color: #95a5a6; margin-right: 5px; }\n" +
        "</style>";

    public JobDetailsPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder("Job Details"),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));

        detailsPane = new JEditorPane();
        detailsPane.setEditable(false);
        detailsPane.setContentType("text/html");
        detailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        detailsPane.setFont(new Font("Arial", Font.PLAIN, 12));
        detailsPane.setBackground(new Color(250, 250, 250));

        // Add hyperlink listener
        detailsPane.addHyperlinkListener(e -> {
            if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                try {
                    Desktop.getDesktop().browse(new URI(e.getURL().toString()));
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                        "Error opening link: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        // Create scroll pane with custom styling
        JScrollPane scrollPane = new JScrollPane(detailsPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
        scrollPane.getViewport().setBackground(Color.WHITE);

        add(scrollPane, BorderLayout.CENTER);
    }

    public void setDetails(String details) {
        if (details == null || details.trim().isEmpty()) {
            detailsPane.setText("<html>" + CSS + "<body><i>No details available</i></body></html>");
            return;
        }

        String[] lines = details.split("\n");
        StringBuilder html = new StringBuilder();
        html.append("<html>").append(CSS).append("<body>");

        String currentSection = "";
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.endsWith(":")) {
                // Close previous section if exists
                if (!currentSection.isEmpty()) {
                    html.append("</div>");
                }
                // Start new section
                currentSection = line.substring(0, line.length() - 1);
                html.append("<h2>").append(currentSection).append("</h2>");
                html.append("<div class='section'>");
            } else if (line.contains(": ")) {
                String[] parts = line.split(": ", 2);
                String label = parts[0].trim();
                String value = parts[1].trim();

                html.append("<div>");
                html.append("<span class='label'>").append(label).append(": </span>");

                // Format based on content type
                if (label.equals("Apply at") || label.equals("Company Website")) {
                    html.append("<a href='").append(value).append("' class='link'>")
                        .append(value).append("</a>");
                } else if (label.contains("Date")) {
                    html.append("<span class='date'>").append(value).append("</span>");
                } else if (label.equals("Remote Work") || label.equals("Internship")) {
                    html.append("<span class='highlight'>").append(value).append("</span>");
                } else if (label.contains("Skills") || label.equals("Diploma") || 
                         label.equals("Languages") || label.equals("Personality Traits")) {
                    html.append("<div class='skills'>").append(formatList(value)).append("</div>");
                } else {
                    html.append("<span class='value'>").append(value).append("</span>");
                }
                html.append("</div>");
            } else {
                html.append("<div class='value'>").append(line).append("</div>");
            }
        }

        // Close last section if exists
        if (!currentSection.isEmpty()) {
            html.append("</div>");
        }

        html.append("</body></html>");
        detailsPane.setText(html.toString());
        detailsPane.setCaretPosition(0);
    }

    private String formatList(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "<i>None specified</i>";
        }

        // Remove brackets if present
        value = value.replaceAll("[\\[\\]]", "");
        
        // Split by commas and create bullet points
        String[] items = value.split(",");
        StringBuilder formatted = new StringBuilder();
        for (String item : items) {
            item = item.trim();
            if (!item.isEmpty()) {
                formatted.append("<span class='bullet-point'>•</span>")
                        .append(item)
                        .append("<br>");
            }
        }
        return formatted.toString();
    }
}
