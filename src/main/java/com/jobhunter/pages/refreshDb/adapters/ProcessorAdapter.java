package com.jobhunter.pages.refreshDb.adapters;

import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;

public class ProcessorAdapter implements DataProcessor {
    private final Object processor;
    private final Class<?> processorClass;

    public ProcessorAdapter(Class<?> processorClass) {
        try {
            this.processorClass = processorClass;
            this.processor = processorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create processor instance: " + e.getMessage());
        }
    }

    @Override
    public void execute() throws Exception {
        try {
            // First try to find and invoke a 'processAllJobOffers' method (for RegExCleaner)
            try {
                Method processMethod = processorClass.getMethod("processAllJobOffers");
                processMethod.invoke(processor);
                return;
            } catch (NoSuchMethodException e1) {
                // Then try 'process' method
                try {
                    Method processMethod = processorClass.getMethod("process");
                    processMethod.invoke(processor);
                    return;
                } catch (NoSuchMethodException e2) {
                    // Finally try 'main' method
                    try {
                        Method mainMethod = processorClass.getMethod("main", String[].class);
                        mainMethod.invoke(null, (Object) new String[0]);
                    } catch (NoSuchMethodException e3) {
                        throw new Exception("No suitable execution method found in " + processorClass.getSimpleName());
                    }
                }
            }
        } catch (Exception e) {
            String errorMsg = e.getMessage() != null ? e.getMessage() : "Unknown error occurred";
            throw new Exception("Failed to execute processor " + processorClass.getSimpleName() + ": " + errorMsg, e);
        }
    }

    @Override
    public void setDatabasePath(String path) {
        try {
            // Try to set DATABASE_PATH field if it exists
            try {
                Field dbPathField = processorClass.getDeclaredField("DATABASE_PATH");
                dbPathField.setAccessible(true);
                dbPathField.set(null, path); // static field
            } catch (NoSuchFieldException e) {
                // If DATABASE_PATH doesn't exist, try to set databasePath field
                Field dbPathField = processorClass.getDeclaredField("databasePath");
                dbPathField.setAccessible(true);
                dbPathField.set(processor, path);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not set database path: " + e.getMessage());
        }
    }

    @Override
    public void setAppendMode(boolean appendMode) {
        try {
            // Try to set APPEND_MODE field if it exists
            try {
                Field appendModeField = processorClass.getDeclaredField("APPEND_MODE");
                appendModeField.setAccessible(true);
                appendModeField.set(null, appendMode); // static field
            } catch (NoSuchFieldException e) {
                // If APPEND_MODE doesn't exist, try to set appendMode field
                Field appendModeField = processorClass.getDeclaredField("appendMode");
                appendModeField.setAccessible(true);
                appendModeField.set(processor, appendMode);
            }
        } catch (Exception e) {
            System.err.println("Warning: Could not set append mode: " + e.getMessage());
        }
    }
}
