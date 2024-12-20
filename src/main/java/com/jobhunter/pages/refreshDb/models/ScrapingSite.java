package com.jobhunter.pages.refreshDb.models;

public class ScrapingSite {
    private String name;
    private boolean selected;
    private int pageCount;

    public ScrapingSite(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
        this.pageCount = 5; // Default page count
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }
}
