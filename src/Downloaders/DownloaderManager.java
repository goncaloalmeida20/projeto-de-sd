package Downloaders;

import classes.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloaderManager {
    public static final int MAX_PAGE_LIST_SIZE = 100;
    public static final List<Page> pageQueue = Collections.synchronizedList(new ArrayList<Page>());
    public static void main(String[] args) {
        Downloader d = new Downloader(Integer.parseInt(args[0]));
        DownloaderMulticast dm = new DownloaderMulticast(Integer.parseInt(args[0]));
    }
}
