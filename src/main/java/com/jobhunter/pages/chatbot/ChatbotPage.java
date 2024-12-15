package com.jobhunter.pages.chatbot;

import javax.swing.*;
import java.awt.*;

public class ChatbotPage {
    private JFrame frame;

    public ChatbotPage() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Chatbot");
        frame.setBounds(100, 100, 400, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JLabel label = new JLabel("This is the Chatbot Page", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.PLAIN, 18));
        frame.add(label, BorderLayout.CENTER);
    }

    public void show() {
        frame.setVisible(true);
    }
}
