package Downloaders;

import classes.Page;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DownloaderManager {
    public static final String MULTICAST_ADDRESS = "224.0.1.0";
    public static final int MAX_PAGE_LIST_SIZE = 100, MULTICAST_PORT = 5000;
    public static final List<Page> pageQueue = Collections.synchronizedList(new ArrayList<>()),
            msgBuffer = Collections.synchronizedList(new ArrayList<>());
    public static int seq_number = 0;
    public static void main(String[] args) {
        Downloader d = new Downloader(Integer.parseInt(args[0]));
        DownloaderMulticastWorker dm = new DownloaderMulticastWorker(Integer.parseInt(args[0]));
    }
}
