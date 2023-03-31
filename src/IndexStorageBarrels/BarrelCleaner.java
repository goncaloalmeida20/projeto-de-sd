package IndexStorageBarrels;


import classes.TimedByteBuffer;

import java.util.ArrayList;

public class BarrelCleaner implements Runnable {
    public int id;
    public Thread t;

    public static long CHECK_INTERVAL_MS = 5 * TimedByteBuffer.TIMEOUT_MS;
    public static long DELETE_BY_TIMEOUT_MS = 5 * TimedByteBuffer.TIMEOUT_MS;
    public BarrelCleaner(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("BarrelCleaner " + id);
        while(true){
            try {
                //Check for timeouts each CHECK_INTERVAL_MS ms
                Thread.sleep(CHECK_INTERVAL_MS);

                //check for aheadBuffer timeouts
                synchronized (BarrelMulticastWorker.aheadBuffer){
                    var setCopy = new ArrayList<>(BarrelMulticastWorker.aheadBuffer.entrySet());
                    for (var downloaderEntry: setCopy){
                        var downloaderSetCopy = new ArrayList<>(downloaderEntry.getValue().entrySet());
                        for(var seqNumberEntry: downloaderSetCopy){
                            if(seqNumberEntry.getValue().entrySet().iterator().next().getValue().timeSinceCreation()
                                    > DELETE_BY_TIMEOUT_MS){
                                System.out.println("Cleaning aheadBuffer " + seqNumberEntry.getKey());
                                BarrelMulticastWorker.aheadBuffer.remove(seqNumberEntry.getKey());
                            }
                        }
                    }
                }
                //check for downloadersByteBuffers timeouts
                synchronized (BarrelMulticastWorker.downloadersByteBuffers){
                    var setCopy = new ArrayList<>(BarrelMulticastWorker.downloadersByteBuffers.entrySet());
                    for (var downloaderEntry: setCopy){
                        var downloader = downloaderEntry.getKey();
                        var downloaderSetCopy = new ArrayList<>(downloaderEntry.getValue().entrySet());
                        for(var seqNumberEntry: downloaderSetCopy){
                            int seqNumber = seqNumberEntry.getKey();
                            if(seqNumberEntry.getValue().timeSinceCreation() > DELETE_BY_TIMEOUT_MS){
                                System.out.println("Cleaning downloadersByteBuffers " +
                                        downloader + " " + seqNumber);
                                BarrelMulticastWorker.downloadersByteBuffers.get(downloader).remove(seqNumber);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("BarrelCleaner " + id + " exception: " + e + " - " + e.getMessage());
            }
        }
    }
}
