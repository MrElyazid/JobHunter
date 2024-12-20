package com.jobhunter.pages.refreshDb.factories;

import com.jobhunter.Cleaner.CleanLLM;
import com.jobhunter.Cleaner.RegExCleaner;
import com.jobhunter.Cleaner.Cleaner;
import com.jobhunter.database.InsertJson;
import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;
import com.jobhunter.pages.refreshDb.adapters.ProcessorAdapter;

public class ProcessorFactory {
    public enum ProcessorType {
        REGEX_CLEANER,
        LLM_CLEANER,
        DATABASE_INSERTER
    }

    public static DataProcessor createProcessor(ProcessorType type) {
        Class<?> processorClass;
        switch (type) {
            case REGEX_CLEANER:
                processorClass = RegExCleaner.class;
                break;
            case LLM_CLEANER:
                processorClass = Cleaner.class;
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
