package com.jobhunter.pages.refreshDb.factories;

import com.jobhunter.LinksScraper.*;
import com.jobhunter.DataScraper.*;
import com.jobhunter.pages.refreshDb.interfaces.Scraper;
import com.jobhunter.pages.refreshDb.adapters.ScraperAdapter;

public class ScraperFactory {
    public static Scraper createLinksScraper(String siteName) {
        Class<?> scraperClass;
        switch (siteName) {
            case "Rekrute":
                scraperClass = Rekrute.class;
                break;
            case "Anapec":
                scraperClass = Anapec.class;
                break;
            case "EmploiMa":
                scraperClass = EmploiMa.class;
                break;
            case "KhdmaMa":
                scraperClass = KhdmaMa.class;
                break;
            case "MarocAnnonces":
                scraperClass = MarocAnnonces.class;
                break;
            case "MonCallCenter":
                scraperClass = MonCallCenter.class;
                break;
            case "StagairesMa":
                scraperClass = StagairesMa.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown site: " + siteName);
        }
        return new ScraperAdapter(scraperClass);
    }

    public static Scraper createDataScraper(String siteName) {
        Class<?> scraperClass;
        switch (siteName) {
            case "Rekrute":
                scraperClass = RekruteScraper.class;
                break;
            case "Anapec":
                scraperClass = AnapecSc.class;
                break;
            case "EmploiMa":
                scraperClass = EmploiMaSc.class;
                break;
            case "KhdmaMa":
                scraperClass = KhdmaMaSc.class;
                break;
            case "MarocAnnonces":
                scraperClass = MarocAnnoncesSc.class;
                break;
            case "MonCallCenter":
                scraperClass = MonCallCenterSc.class;
                break;
            case "StagairesMa":
                scraperClass = StagiairesScraper.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown site: " + siteName);
        }
        return new ScraperAdapter(scraperClass);
    }
}
