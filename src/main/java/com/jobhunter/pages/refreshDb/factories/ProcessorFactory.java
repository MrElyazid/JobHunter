package com.jobhunter.pages.refreshDb.factories;

import com.jobhunter.Cleaner.CleanLLM;
import com.jobhunter.database.InsertJson;
import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import com.jobhunter.pages.refreshDb.adapters.ProcessorAdapter;

public class ProcessorFactory {
    public enum ProcessorType {
        CLEANER,
        DATABASE_INSERTER
    }

    public static DataProcessor createProcessor(ProcessorType type) {
        Class<?> processorClass;
        switch (type) {
            case CLEANER:
                processorClass = CleanLLM.class;
                break;
            case DATABASE_INSERTER:
                processorClass = InsertJson.class;
                break;
            default:
                throw new IllegalArgumentException("Unknown processor type: " + type);
        }
        return new ProcessorAdapter(processorClass);
    }
}
