package com.jobhunter.pages.refreshDb.adapters;

import com.jobhunter.pages.refreshDb.interfaces.DataProcessor;

public class ProcessorAdapter implements DataProcessor {
    private final Class<?> processorClass;

    public ProcessorAdapter(Class<?> processorClass) {
        this.processorClass = processorClass;
    }

    @Override
    public void execute() throws Exception {
        // Call the main method of the processor class using reflection
        processorClass.getMethod("main", String[].class)
                     .invoke(null, (Object) new String[0]);
    }
}
