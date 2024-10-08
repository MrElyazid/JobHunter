package com.jobhunter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;

import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {
    public static void saveJsonToFile(JsonArray jsonArray, String fileName) {
        try (FileWriter file = new FileWriter(fileName)) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(jsonArray, file);
            System.out.println("Data has been saved to " + fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

