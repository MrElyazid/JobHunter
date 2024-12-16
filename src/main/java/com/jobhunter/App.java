package com.jobhunter;

import java.awt.EventQueue;
import com.jobhunter.pages.main.MainPage;

public class App {
    public static void main(String[] args) {
        EventQueue.invokeLater(() -> {
            try {
                // Set system look and feel
                javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
                );
                
                // Launch main page
                MainPage mainPage = MainPage.getInstance();
                mainPage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
