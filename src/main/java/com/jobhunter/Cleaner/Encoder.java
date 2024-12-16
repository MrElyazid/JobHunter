package com.jobhunter.Cleaner;

import java.io.*;
import java.nio.file.*;
import org.json.*;

public class Encoder {
    private static final String DIRECTORY_PATH = "lastScrappe/";

    public static void main(String[] args) {
        try {
            Files.list(Paths.get(DIRECTORY_PATH))
                .filter(path -> path.toString().endsWith(".json"))
                .forEach(Encoder::processFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void processFile(Path filePath) {
        try {
            String content = new String(Files.readAllBytes(filePath));
            String correctedContent = fixSpecialCharacters(content);
            Files.write(filePath, correctedContent.getBytes());
            System.out.println("Processed: " + filePath.getFileName());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String fixSpecialCharacters(String content) {
        // Replace problematic characters with correct ones
        return content.replace("é", "e")
                      .replace("è", "e")
                      .replace("ê", "e");
    }
}
