package Downloaders;

import classes.Page;

import java.rmi.RemoteException;
import java.util.*;

public class DownloaderManager {
    public static final String MULTICAST_ADDRESS = "224.0.1.0";
    public static final int MAX_PAGE_LIST_SIZE = 100, MULTICAST_PORT = 5000;
    public static final List<Page> pageQueue = Collections.synchronizedList(new ArrayList<>());
    public static final Map<Integer, Page> pageBuffer = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Page, Long> recoveredPages = Collections.synchronizedMap(new HashMap<>());
    public static int seqNumber = 0;

    public AdminDownloader adminDownloader;
    Thread t1;

    public DownloaderManager() throws RemoteException {
        adminDownloader = new AdminDownloader();
        t1 = new Thread(adminDownloader);
        t1.start();
    }

    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        try {
            new DownloaderManager();
        } catch (RemoteException e) {
            System.out.println("Error in adminDownloader");
        }
        Downloader d = new Downloader(id);
        DownloaderMulticastReceiver dmrcv = new DownloaderMulticastReceiver(id);
        DownloaderMulticastWorker dmw = new DownloaderMulticastWorker(id);
        DownloaderMulticastRecovery dmr = new DownloaderMulticastRecovery(id);

    }
}
