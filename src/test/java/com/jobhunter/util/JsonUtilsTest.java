package com.jobhunter.util;

import static org.junit.Assert.*;
import org.junit.Test;
import org.junit.Before;
import org.junit.After;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;

public class JsonUtilsTest {
    private static final String TEST_FILE_PATH = "src/test/resources/test.json";

    @Before
    public void setUp() {
        // Create test directory if it doesn't exist
        new File("src/test/resources").mkdirs();
    }

    @After
    public void tearDown() {
        // Clean up test file after tests
        File testFile = new File(TEST_FILE_PATH);
        if (testFile.exists()) {
            testFile.delete();
        }
    }

    @Test
    public void testSaveAndReadJsonArray() throws IOException {
        // Create test data
        JsonArray testArray = new JsonArray();
        JsonObject testObject = new JsonObject();
        testObject.addProperty("title", "Test Job");
        testObject.addProperty("company", "Test Company");
        testArray.add(testObject);

        // Test saving
        JsonUtils.saveJsonToFile(testArray, TEST_FILE_PATH);
        assertTrue("Test file should be created", new File(TEST_FILE_PATH).exists());

        // Test reading
        JsonArray readArray = JsonUtils.readJsonArrayFromFile(TEST_FILE_PATH);
        assertNotNull("Read array should not be null", readArray);
        assertEquals("Array should have one element", 1, readArray.size());
        
        JsonObject readObject = readArray.get(0).getAsJsonObject();
        assertEquals("Title should match", "Test Job", readObject.get("title").getAsString());
        assertEquals("Company should match", "Test Company", readObject.get("company").getAsString());
    }

    @Test(expected = IOException.class)
    public void testReadNonExistentFile() throws IOException {
        JsonUtils.readJsonArrayFromFile("nonexistent.json");
    }
}
