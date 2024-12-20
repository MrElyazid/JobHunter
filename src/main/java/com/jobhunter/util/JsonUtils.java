package com.jobhunter.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JsonUtils {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static JsonArray readJsonArrayFromFile(String filePath) throws IOException {
        FileReader reader = new FileReader(filePath);
        return JsonParser.parseReader(reader).getAsJsonArray();
    }

    public static void saveJsonToFile(JsonArray jsonArray, String filePath) {
        try (FileWriter file = new FileWriter(filePath)) {
            file.write(jsonArray.toString());
            file.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}