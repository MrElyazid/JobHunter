package com.jobhunter.DynamicScrapers;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.openqa.selenium.WebDriver;
// import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.chrome.ChromeDriver;
import io.github.bonigarcia.wdm.WebDriverManager;

import com.jobhunter.util.JsonUtils;

public class Indeed {

    private static final String BASE_URL = "https://ma.indeed.com/jobs";
    private String keyword;
    private String city;
    private int maxPages;

    public Indeed(String keyword, String city, int maxPages) {
        this.keyword = keyword.replace(" ", "+");
        this.city = city.replace(" ", "+");
        this.maxPages = maxPages;
    }

    public void scrape() {
        JsonArray jobsArray = new JsonArray();
        WebDriver driver = null;

        try {
            //we're using webDriver instead of manually setting up chromeDriver
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless"); //run in headless mode
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            driver = new ChromeDriver(options);

            for (int i = 0; i < maxPages; i++) {
                String url = BASE_URL + "?q=" + keyword + "&l=" + city + "&start=" + (i * 10);

                try {
                    
                    driver.get(url);
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    System.out.println("Sleep interrupted: " + e.getMessage());
                    continue;
                }

                String pageSource = driver.getPageSource();
                Document doc = Jsoup.parse(pageSource);

                Elements jobListings = doc.select("li.css-1ac2h1w.eu4oa1w0");

                for (Element job : jobListings) {
                    Element linkElement = job.selectFirst("h2.jobTitle a");
                    if (linkElement != null) {
                        String title = linkElement.text().replace("\"", "'");
                        String link = "https://ma.indeed.com" + linkElement.attr("href");

                        JsonObject jobJson = new JsonObject();
                        jobJson.addProperty("title", title);
                        jobJson.addProperty("link", link);

                        jobsArray.add(jobJson);
                    }
                }
            }

            String outputFileName = "data/IndeedLinks_" + keyword + "_" + city + ".json";
            JsonUtils.saveJsonToFile(jobsArray, outputFileName);
            System.out.println("Indeed scraping completed. Results saved to " + outputFileName);

        } finally {
            if (driver != null) {
                driver.quit();
            }
        }
    }
}
