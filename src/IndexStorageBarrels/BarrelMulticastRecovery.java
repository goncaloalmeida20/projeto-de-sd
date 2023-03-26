package IndexStorageBarrels;

import classes.TimedByteBuffer;

import java.net.*;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BarrelMulticastRecovery implements Runnable{
    private static final String MULTICAST_ADDRESS = "224.0.1.0";
    private static final int MULTICAST_PORT = 5000;
    public int id;
    public Thread t;

    public BarrelMulticastRecovery(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    /**
     * @return 0 if no out of order packets were found,
     * otherwise the time left for the next timeout
     */
    private long searchOutOfOrderPackets(){
        //check if there are buffered packets
        //System.out.println("BMR SOOOP");
        long minWaitTime = 0;
        synchronized (BarrelMulticastWorker.aheadBuffer){
            List<List<Integer>> removeFromAheadBuffer = new ArrayList<>();
            for(var downloaderSet: BarrelMulticastWorker.aheadBuffer.entrySet()){
                int downloader = downloaderSet.getKey();
                for(var seqNumberSet: BarrelMulticastWorker.aheadBuffer.get(downloader).entrySet()){
                    int seqNumber = seqNumberSet.getKey();
                    for(var msgsLeftSet: BarrelMulticastWorker.aheadBuffer.get(downloader).get(seqNumber)
                            .entrySet()){
                        long timeTemp = TimedByteBuffer.TIMEOUT_MS - msgsLeftSet.getValue().timeSinceCreation();
                        if(timeTemp > 0 && (minWaitTime == 0 || timeTemp < minWaitTime)){
                            minWaitTime = timeTemp;
                        }
                    }
                }
            }
        }

        //check if there are any seqNumbers ahead of the counter (lastSeqNumber)
        synchronized (BarrelMulticastWorker.lastSeqNumber){
            for(var downloaderSet: BarrelMulticastWorker.lastSeqNumber.entrySet()){
                int downloader = downloaderSet.getKey(), lastSeqNumber = downloaderSet.getValue();
                synchronized (BarrelMulticastWorker.downloadersByteBuffers){
                    for(var seqNumberSet: BarrelMulticastWorker.downloadersByteBuffers.get(downloader).entrySet()){
                        if(seqNumberSet.getKey() > lastSeqNumber){
                            long timeTemp = TimedByteBuffer.TIMEOUT_MS - seqNumberSet.getValue().timeSinceCreation();
                            if(timeTemp > 0 && (minWaitTime == 0 || timeTemp < minWaitTime)){
                                minWaitTime = timeTemp;
                            }
                        }
                    }
                }
            }
        }
        return minWaitTime;
    }

    public void run(){
        System.out.println("BarrelMulticastRecovery " + id);
        try (MulticastSocket socket = new MulticastSocket()) {
            long minWaitTime = 0; //time to wait for next out of order packet timeout
            while(true){
                //System.out.println("BMR1");
                synchronized (BarrelMulticastWorker.aheadBuffer){
                    //System.out.println("BMR2");
                    minWaitTime = searchOutOfOrderPackets();
                    while(minWaitTime == 0){
                        BarrelMulticastWorker.aheadBuffer.wait();
                        minWaitTime = searchOutOfOrderPackets();
                        //System.out.println("BMR " + aheadError);
                    }
                }
                //Wait for the next out of order packet timeout
                Thread.sleep(minWaitTime);

                List<List<Integer>> nacks = new ArrayList<>();

                //get seqNumbers ahead of the counter (lastSeqNumber)
                synchronized (BarrelMulticastWorker.lastSeqNumber){
                    for(var downloaderSet: BarrelMulticastWorker.lastSeqNumber.entrySet()){
                        int downloader = downloaderSet.getKey(), lastSeqNumber = downloaderSet.getValue(),
                            newLastSeqNumber = 0;
                        synchronized (BarrelMulticastWorker.downloadersByteBuffers){
                            //System.out.println("BMR " + downloader);
                            for(var seqNumberSet: BarrelMulticastWorker.downloadersByteBuffers.get(downloader).entrySet()){
                                int currentSeqNumber = seqNumberSet.getKey();
                                if(currentSeqNumber > lastSeqNumber){
                                    TimedByteBuffer tbb = seqNumberSet.getValue();
                                    List<Integer> currentPossibleNack = Arrays.asList(downloader, lastSeqNumber+1);
                                    if(tbb.timeSinceCreation() > TimedByteBuffer.TIMEOUT_MS &&
                                        !nacks.contains(currentPossibleNack)){
                                        if(newLastSeqNumber == 0 || currentSeqNumber < newLastSeqNumber){
                                            newLastSeqNumber = currentSeqNumber;
                                        }
                                        //System.out.println("PUT " + downloader + " " + newLastSeqNumber);
                                        nacks.add(currentPossibleNack);
                                    }
                                }
                            }
                        }
                        //System.out.println("PUT " + downloader + " " + newLastSeqNumber
                        //        + " " + BarrelMulticastWorker.lastSeqNumber.get(downloader));
                        if(newLastSeqNumber != 0)
                            BarrelMulticastWorker.lastSeqNumber.put(downloader, newLastSeqNumber);
                    }
                }

                //get buffered packets
                synchronized (BarrelMulticastWorker.aheadBuffer){
                    //System.out.println("BMR HELLO0");
                    List<List<Integer>> removeFromAheadBuffer = new ArrayList<>();
                    for(var downloaderSet: BarrelMulticastWorker.aheadBuffer.entrySet()){
                        int downloader = downloaderSet.getKey();
                        //System.out.println("BMR HELLO1");
                        for(var seqNumberSet: BarrelMulticastWorker.aheadBuffer.get(downloader).entrySet()){
                            int seqNumber = seqNumberSet.getKey();
                            //System.out.println("BMR HELLO2");
                            for(var msgsLeftSet: BarrelMulticastWorker.aheadBuffer.get(downloader).get(seqNumber)
                                    .entrySet()){
                                int msgsLeft = msgsLeftSet.getKey();
                                //System.out.println("BMR HELLO3");
                                TimedByteBuffer tbb = msgsLeftSet.getValue();
                                synchronized (BarrelMulticastWorker.lastMsgsLeft){
                                    //if a there is a buffered packet in the current seqNumber of the current
                                    //downloader and it has timed out, register NACK to send to downloader
                                    List<Integer> currentPossibleNack = Arrays.asList(downloader, seqNumber);
                                    /*for(var nack: nacks){
                                        System.out.println("NACKTEMP " + nack[0] + " " + nack[1] + " " + msgsLeft);
                                        System.out.println(nacks.contains(currentPossibleNack));
                                        System.out.println(currentPossibleNack.equals(nack));
                                    }*/
                                    if (msgsLeft < BarrelMulticastWorker.lastMsgsLeft.get(downloader).get(seqNumber)
                                        - 1 && tbb.timeSinceCreation() > TimedByteBuffer.TIMEOUT_MS &&
                                        !nacks.contains(currentPossibleNack)) {

                                        removeFromAheadBuffer.add(currentPossibleNack);
                                        nacks.add(currentPossibleNack);
                                    }
                                }
                                //System.out.println("BMR HELLO4");
                            }
                        }
                    }
                    /*for(var nack: nacks){
                        System.out.println("NACKTEMP2 " + nack.get(0) + " " + nack.get(1));
                    }*/
                    for(var nack: removeFromAheadBuffer){
                        //System.out.println("NACKS " + nack.get(0) + " " + nack.get(1));
                        BarrelMulticastWorker.aheadBuffer.get(nack.get(0)).remove(nack.get(1));
                        if(BarrelMulticastWorker.aheadBuffer.get(nack.get(0)).isEmpty())
                            BarrelMulticastWorker.aheadBuffer.remove(nack.get(0));
                        //System.out.println("NACKS2 " + nack.get(0) + " " + nack.get(1));
                    }
                }
                for(var nack: nacks){
                    System.out.println("NACK " + nack.get(0) + " " + nack.get(1));
                }

                //System.out.println("BMR HELLO5");
            }


            /*
            //Send the packet
            InetAddress group = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
            DatagramPacket packet = new DatagramPacket(packet_buffer, packet_buffer.length, group,
            DownloaderManager.MULTICAST_PORT);
            socket.send(packet);
            */
        } catch (Exception e) {
            System.out.println("BarrelMulticastRecovery " + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
