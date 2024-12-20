package com.jobhunter.pages.refreshDb.interfaces;

public interface DataProcessor {
    void execute() throws Exception;
    void setDatabasePath(String path);
    void setAppendMode(boolean appendMode);
}
