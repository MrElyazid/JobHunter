package com.jobhunter.pages.browse.panels;

import javax.swing.*;
import java.awt.*;

public class JobDetailsPanel extends JPanel {
    private JTextArea detailsArea;

    public JobDetailsPanel() {
        initialize();
    }

    private void initialize() {
        setPreferredSize(new Dimension(getWidth(), 200));
        setBorder(BorderFactory.createTitledBorder("Job Details"));
        setLayout(new BorderLayout());

        detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setLineWrap(true);
        detailsArea.setMargin(new Insets(5, 5, 5, 5));

        add(new JScrollPane(detailsArea), BorderLayout.CENTER);
    }

    public void setDetails(String details) {
        detailsArea.setText(details);
        detailsArea.setCaretPosition(0);
    }
}
