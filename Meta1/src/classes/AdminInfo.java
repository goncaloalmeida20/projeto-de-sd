package classes;

import java.io.Serializable;

public class AdminInfo implements Serializable {
    private int numDownloads;
    private int numActiveBarrels;
    private String mostSearchedItems;

    public AdminInfo() {
        this.numDownloads = 0;
        this.numActiveBarrels = 0;
        this.mostSearchedItems = null;
    }

    public AdminInfo(int numDownloads, int numActiveBarrels, String mostSearchedItems) {
        this.numDownloads = numDownloads;
        this.numActiveBarrels = numActiveBarrels;
        this.mostSearchedItems = mostSearchedItems;
    }

    public String getNumDownloads() {
        return Integer.toString(numDownloads);
    }

    public void setNumDownloads(int numDownloads) {
        this.numDownloads = numDownloads;
    }

    public String getNumActiveBarrels() {
        return Integer.toString(numActiveBarrels);
    }

    public void setNumActiveBarrels(int numActiveBarrels) {
        this.numActiveBarrels = numActiveBarrels;
    }

    public String getMostSearchedItems() {
        return mostSearchedItems;
    }

    public void setMostSearchedItems(String mostSearchedItems) {
        this.mostSearchedItems = mostSearchedItems;
    }
}
