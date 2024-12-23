package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.Desktop;
import java.net.URI;

public class JobDetailsPanel extends JPanel {
    private JEditorPane detailsPane;
    private static final String CSS = 
        "<style>\n" +
        "    body { font-family: 'Segoe UI', sans-serif; margin: 15px; line-height: 1.5; color: #333; }\n" +
        "    h2 { color: #1976D2; margin: 20px 0 10px; font-size: 16px; font-weight: 600; }\n" +
        "    .section { margin-bottom: 15px; background: #fff; padding: 15px; border-radius: 4px; box-shadow: 0 1px 3px rgba(0,0,0,0.1); }\n" +
        "    .label { color: #666; font-weight: 600; font-size: 13px; }\n" +
        "    .value { color: #333; font-size: 13px; }\n" +
        "    .skills { margin: 8px 0 8px 20px; }\n" +
        "    .link { color: #1976D2; text-decoration: none; border-bottom: 1px solid #1976D2; }\n" +
        "    .date { color: #D32F2F; font-weight: 600; }\n" +
        "    .highlight { background-color: #E3F2FD; color: #1976D2; padding: 3px 8px; border-radius: 4px; font-weight: 600; }\n" +
        "    .bullet-point { color: #1976D2; margin-right: 8px; }\n" +
        "    .description { background: #FAFAFA; padding: 12px; border-left: 3px solid #1976D2; margin: 10px 0; }\n" +
        "</style>";

    public JobDetailsPanel() {
        initialize();
    }

    private void initialize() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createTitledBorder(null, "Job Details",
                TitledBorder.DEFAULT_JUSTIFICATION,
                TitledBorder.DEFAULT_POSITION,
                new Font("Segoe UI", Font.BOLD, 14),
                new Color(25, 118, 210)),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        detailsPane = new JEditorPane();
        detailsPane.setEditable(false);
        detailsPane.setContentType("text/html");
        detailsPane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        detailsPane.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

        // Create scroll pane with modern styling
        JScrollPane scrollPane = new JScrollPane(detailsPane);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Color.WHITE);

        // Customize the scrollbar
        JScrollBar verticalBar = scrollPane.getVerticalScrollBar();
        verticalBar.setUnitIncrement(16);
        verticalBar.setPreferredSize(new Dimension(10, 0));

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
