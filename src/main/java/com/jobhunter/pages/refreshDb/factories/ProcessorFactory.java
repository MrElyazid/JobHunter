package com.jobhunter.pages.refreshDb.factories;

import java.util.List;
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

    public static DataProcessor createProcessor(ProcessorType type, Object... args) {
        Class<?> processorClass;
        switch (type) {
            case REGEX_CLEANER:
                if (args.length > 0 && args[0] instanceof List) {
                    return new RegExCleaner((List<String>) args[0]);
                }
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
