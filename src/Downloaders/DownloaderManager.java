package Downloaders;

import java.util.*;

public class DownloaderManager {
    public static final int NUMBER_OF_DOWNLOADERS = 10;

    private static List<Downloader> downloaderList;
    public static void main(String[] args) {
        downloaderList = new ArrayList<Downloader>();
        for (int i = 0; i < NUMBER_OF_DOWNLOADERS; i++) {
            downloaderList.add(new Downloader(i));
        }
    }
}
