package com.jobhunter.util;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonUtils {
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
