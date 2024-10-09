package com.jobhunter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import com.jobhunter.scraper.StagairesMa;
import com.jobhunter.scraper.Rekrute;
import com.jobhunter.scraper.MarocAnnonces;
import com.jobhunter.scraper.MonCallCenter;
import com.jobhunter.scraper.EmploiMa;

public class App {
    private JFrame frame;
    private JButton stagairesMaButton;
    private JButton rekruteButton;
    private JButton marocAnnoncesButton;
    private JButton monCallCenterButton;
    private JButton emploiMaButton;
    private JButton quitButton;

    public App() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Job Scraper");
        frame.setBounds(100, 100, 300, 300);  // Adjusted the height to accommodate the new button
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(6, 1, 10, 10));  // Changed to 6 rows

        stagairesMaButton = new JButton("Run StagairesMa Scraper");
        rekruteButton = new JButton("Run Rekrute Scraper");
        marocAnnoncesButton = new JButton("Run MarocAnnonces Scraper");
        monCallCenterButton = new JButton("Run MonCallCenter Scraper");
        emploiMaButton = new JButton("Run EmploiMa Scraper");  // New button
        quitButton = new JButton("Quit");

        frame.add(stagairesMaButton);
        frame.add(rekruteButton);
        frame.add(marocAnnoncesButton);
        frame.add(monCallCenterButton);
        frame.add(emploiMaButton);  // Added EmploiMa button
        frame.add(quitButton);

        stagairesMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("StagairesMa");
            }
        });

        rekruteButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("Rekrute");
            }
        });

        marocAnnoncesButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("MarocAnnonces");
            }
        });

        monCallCenterButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("MonCallCenter");
            }
        });

        emploiMaButton.addActionListener(new ActionListener() {  // Added action for EmploiMaScraper
            public void actionPerformed(ActionEvent e) {
                runScraper("EmploiMa");
            }
        });

        quitButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
    }

    private void runScraper(String scraperName) {
        SwingWorker<Void, Void> worker = new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                switch (scraperName) {
                    case "StagairesMa":
                        new StagairesMa().scrape();
                        break;
                    case "Rekrute":
                        new Rekrute().scrape();
                        break;
                    case "MarocAnnonces":
                        new MarocAnnonces().scrape();
                        break;
                    case "MonCallCenter":
                        new MonCallCenter().scrape();
                        break;
                    case "EmploiMa":  // New case for EmploiMaScraper
                        new EmploiMa().scrape();
                        break;
                }
                return null;
            }

            @Override
            protected void done() {
                JOptionPane.showMessageDialog(frame, scraperName + " scraping is done. Output saved to the data folder.");
            }
        };
        worker.execute();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    App window = new App();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
