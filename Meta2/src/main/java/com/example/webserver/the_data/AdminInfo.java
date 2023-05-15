package com.example.webserver.the_data;

import java.util.List;

public class AdminInfo {
    private int numDownloads;
    private int numActiveBarrels;
    private List<String> mostSearchedItems;

    public AdminInfo() {
        this.numDownloads = 0;
        this.numActiveBarrels = 0;
        this.mostSearchedItems = null;
    }

    public AdminInfo(int numDownloads, int numActiveBarrels, List<String> mostSearchedItems) {
        this.numDownloads = numDownloads;
        this.numActiveBarrels = numActiveBarrels;
        this.mostSearchedItems = mostSearchedItems;
    }

    public int getNumDownloads() {
        return numDownloads;
    }

    public void setNumDownloads(int numDownloads) {
        this.numDownloads = numDownloads;
    }

    public int getNumActiveBarrels() {
        return numActiveBarrels;
    }

    public void setNumActiveBarrels(int numActiveBarrels) {
        this.numActiveBarrels = numActiveBarrels;
    }

    public List<String> getMostSearchedItems() {
        return mostSearchedItems;
    }

    public void setMostSearchedItems(List<String> mostSearchedItems) {
        this.mostSearchedItems = mostSearchedItems;
    }
}