package com.jobhunter;

import javax.swing.*;

import com.jobhunter.LinksScraper.EmploiMa;
import com.jobhunter.LinksScraper.MarocAnnonces;
import com.jobhunter.LinksScraper.MonCallCenter;
import com.jobhunter.LinksScraper.Rekrute;
import com.jobhunter.LinksScraper.StagairesMa;
import com.jobhunter.LinksScraper.Anapec;
import com.jobhunter.LinksScraper.OffresEmploiMa;
import com.jobhunter.LinksScraper.MJobMa;
import com.jobhunter.LinksScraper.KhdmaMa;

import com.jobhunter.DynamicScrapers.Indeed;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class App {
    private JFrame frame;
    private JButton stagairesMaButton;
    private JButton rekruteButton;
    private JButton marocAnnoncesButton;
    private JButton monCallCenterButton;
    private JButton emploiMaButton;
    private JButton anapecButton;
    private JButton offresEmploiMaButton;
    private JButton mjobMaButton;
    private JButton khdmaMaButton;
    private JButton indeedButton;
    private JButton quitButton;
    
    public App() {
        initialize();
    }

    private void initialize() {
        frame = new JFrame("Job Scraper");
        frame.setBounds(100, 100, 300, 500);  // Adjusted the height to accommodate the new buttons
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new GridLayout(10, 1, 10, 10));  // Changed to 10 rows

        stagairesMaButton = new JButton("Run StagairesMa Scraper");
        rekruteButton = new JButton("Run Rekrute Scraper");
        marocAnnoncesButton = new JButton("Run MarocAnnonces Scraper");
        monCallCenterButton = new JButton("Run MonCallCenter Scraper");
        emploiMaButton = new JButton("Run EmploiMa Scraper");
        anapecButton = new JButton("Run Anapec Scraper");
        offresEmploiMaButton = new JButton("Run OffresEmploiMa Scraper");
        mjobMaButton = new JButton("Run MjobMa Scraper");
        khdmaMaButton = new JButton("Run KhdmaMa Scraper");
        indeedButton = new JButton("Run indeed scraper");
        quitButton = new JButton("Quit");

        frame.add(stagairesMaButton);
        frame.add(rekruteButton);
        frame.add(marocAnnoncesButton);
        frame.add(monCallCenterButton);
        frame.add(emploiMaButton);
        frame.add(anapecButton);
        frame.add(offresEmploiMaButton);
        frame.add(mjobMaButton);
        frame.add(khdmaMaButton);
        frame.add(indeedButton);

        frame.add(quitButton);

        stagairesMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("StagairesMa");
            }
        });


        indeedButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("Indeed");
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

        emploiMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("EmploiMa");
            }
        });

        anapecButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("Anapec");
            }
        });

        offresEmploiMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("OffresEmploiMa");
            }
        });

        mjobMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("MJobMa");
            }
        });

        khdmaMaButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                runScraper("KhdmaMa");
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
                    case "EmploiMa":
                        new EmploiMa().scrape();
                        break;
                    case "Anapec":
                        new Anapec().scrape();
                        break;
                    case "OffresEmploiMa":
                        new OffresEmploiMa().scrape();
                        break;
                    case "MjobMa":
                        new MJobMa().scrape();
                        break;
                    case "KhdmaMa":
                        new KhdmaMa().scrape();
                        break;
                    case "Indeed":
                    Indeed indeedScraper = new Indeed("développement informatique", "Casablanca", 5);
                    indeedScraper.scrape();
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
