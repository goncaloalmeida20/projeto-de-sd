package Downloaders;

import classes.TimedByteBuffer;

import java.util.ArrayList;

public class DownloaderCleaner implements Runnable {
    public int id;
    public Thread t;

    public static long CHECK_INTERVAL_MS = 5 * TimedByteBuffer.TIMEOUT_MS;
    public static long DELETE_BY_TIMEOUT_MS = 5 * TimedByteBuffer.TIMEOUT_MS;
    public DownloaderCleaner(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("DownloaderCleaner " + id);
        while(true){
            try {
                Thread.sleep(CHECK_INTERVAL_MS);
                synchronized (DownloaderManager.pageBuffer){
                    var setCopy = new ArrayList<>(DownloaderManager.pageBuffer.entrySet());
                    for (var seqNumberEntry: setCopy){
                        var seqNumber = seqNumberEntry.getKey();
                        if(System.currentTimeMillis() -
                                seqNumberEntry.getValue().entrySet().iterator().next().getValue()
                                > DELETE_BY_TIMEOUT_MS){
                            System.out.println("Cleaning pageBuffer " +
                                    seqNumberEntry.getValue().entrySet().iterator().next().getKey().url);
                            DownloaderManager.pageBuffer.remove(seqNumber);
                        }
                    }
                }
                synchronized (DownloaderManager.recoveredPages){
                    var keySetCopy = new ArrayList<>(DownloaderManager.recoveredPages.keySet());
                    for (var key: keySetCopy){
                        if(System.currentTimeMillis() - DownloaderManager.recoveredPages.get(key)
                                > DELETE_BY_TIMEOUT_MS){
                            System.out.println("Cleaning recoveredPages " + key.url);
                            DownloaderManager.recoveredPages.remove(key);
                        }
                    }
                }
                synchronized (DownloaderMulticastRecovery.nackPackets){
                    for(int i = 0; i < DownloaderMulticastRecovery.nackPackets.size(); i++){
                        if(DownloaderMulticastRecovery.nackPackets.get(i).timeSinceCreation() > DELETE_BY_TIMEOUT_MS){
                            System.out.println("Cleaning nackPackets " + i);
                            DownloaderMulticastRecovery.nackPackets.remove(i);
                            i--;
                        }
                    }
                }
                synchronized (DownloaderMulticastRecovery.nackackacks){
                    for(int i = 0; i < DownloaderMulticastRecovery.nackackacks.size(); i++){
                        if(DownloaderMulticastRecovery.nackackacks.get(i).timeSinceCreation() > DELETE_BY_TIMEOUT_MS){
                            System.out.println("Cleaned nackackacks " + i);
                            DownloaderMulticastRecovery.nackackacks.remove(i);
                            i--;
                        }
                    }
                }
            } catch (Exception e) {
                System.out.println("DownloaderCleaner " + id + " exception: " + e + " - " + e.getMessage());
            }
        }
    }
}
