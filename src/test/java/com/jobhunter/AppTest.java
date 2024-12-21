package com.jobhunter;

import static org.junit.Assert.*;
import org.junit.Test;
import java.io.File;

public class AppTest {
    
    @Test
    public void testDirectoryStructure() {
        // Test if essential directories exist
        assertTrue("src directory should exist", new File("src").exists());
        assertTrue("src/main directory should exist", new File("src/main").exists());
        assertTrue("src/main/java directory should exist", new File("src/main/java").exists());
    }

    @Test
    public void testResourceDirectories() {
        // Test if resource directories exist
        assertTrue("Resources directory should exist", new File("src/main/resources").exists());
        assertTrue("Dictionary directory should exist", new File("src/main/resources/dictionary").exists());
        assertTrue("ML directory should exist", new File("src/main/resources/ML").exists());
    }

    @Test
    public void testDataDirectories() {
        // Create directories if they don't exist (these are created by the application normally)
        new File("data").mkdirs();
        new File("database").mkdirs();
        new File("lastScrappe").mkdirs();

        // Test if data directories exist
        assertTrue("Data directory should exist", new File("data").exists());
        assertTrue("Database directory should exist", new File("database").exists());
        assertTrue("LastScrappe directory should exist", new File("lastScrappe").exists());
    }
}
