package com.jobhunter.pages.refreshDb.interfaces;

public interface Scraper {
    void execute() throws Exception;
    void setPageCount(int pageCount);
}
