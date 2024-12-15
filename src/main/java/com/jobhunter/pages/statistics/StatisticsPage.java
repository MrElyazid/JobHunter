package com.jobhunter.pages.statistics;

import javax.swing.*;
import java.awt.*;

public class StatisticsPage {
    private JFrame frame;

    public StatisticsPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Statistics and ML");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel("This is the Statistics and ML Page", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        frame.add(label, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }
}
