package Downloaders;

import classes.Page;

import java.io.*;
import java.rmi.RemoteException;
import java.util.*;

public class DownloaderManager {
    public static final String MULTICAST_ADDRESS = "224.0.1.0";
    public static final int MAX_PAGE_LIST_SIZE = 100, MULTICAST_PORT = 5000;
    public static final List<Page> pageQueue = Collections.synchronizedList(new ArrayList<>());
    public static final Map<Integer, Map<Page, Long>> pageBuffer = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Page, Long> recoveredPages = Collections.synchronizedMap(new HashMap<>());
    public static int seqNumber = 0;
    public static int id;
    public AdminDownloader adminDownloader;
    Thread t1;

    /**
     * Update the sequence number and the current downloader sequence number file
     */
    public static void updateSeqNumber(){
        DownloaderManager.seqNumber++;
        File f = new File("downloader" + id + "_sequence_number.dat");
        try{
            //save the sequence number in a file
            FileOutputStream fos = new FileOutputStream(f);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(seqNumber);
            oos.close();
        }
        catch (Exception e){
            System.out.println("Downloader" + id + ": error updating sequence number file");
        }
    }

    /**
     * Recover sequence number from the current downloader sequence number file if it exists
     */
    public static void recoverSeqNumber(){
        File f = new File("downloader" + id + "_sequence_number.dat");
        //check if the file exists
        if(!f.exists()){
            seqNumber = 0;
            return;
        }
        try{
            //read the sequence number from the file
            FileInputStream fis = new FileInputStream(f);
            ObjectInputStream ois = new ObjectInputStream(fis);
            seqNumber = (int) ois.readObject();
            ois.close();
        }
        catch (Exception e){
            System.out.println("Downloader" + id + ": error reading sequence number file");
        }
    }

    public DownloaderManager(int id) throws RemoteException {
        adminDownloader = new AdminDownloader(id);
        t1 = new Thread(adminDownloader);
        t1.start();
    }

    public static void main(String[] args) {
        id = Integer.parseInt(args[0]);
        recoverSeqNumber();
        try {
            new DownloaderManager(id);
        } catch (RemoteException e) {
            System.out.println("Error in adminDownloader");
        }
        Downloader d = new Downloader(id);
        DownloaderMulticastReceiver dmrcv = new DownloaderMulticastReceiver(id);
        DownloaderMulticastWorker dmw = new DownloaderMulticastWorker(id);
        DownloaderMulticastRecovery dmr = new DownloaderMulticastRecovery(id);
        DownloaderCleaner dc = new DownloaderCleaner(id);
    }
}
