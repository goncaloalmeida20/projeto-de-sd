package IndexStorageBarrels;

import Downloaders.DownloaderManager;
import classes.MulticastPacket;
import classes.TimedByteBuffer;

import java.net.*;
import java.nio.ByteBuffer;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BarrelMulticastRecovery implements Runnable{
    //List of NACK ACKs (acknowledges of the NACK messages)
    public static final List<byte[]> nackAcksQueue = Collections.synchronizedList(new ArrayList<>());
    public static final int MAX_RETRIES = 3;

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
        long minWaitTime = -1;
        synchronized (BarrelMulticastWorker.aheadBuffer){
            List<List<Integer>> removeFromAheadBuffer = new ArrayList<>();
            for(var downloaderSet: BarrelMulticastWorker.aheadBuffer.entrySet()){
                int downloader = downloaderSet.getKey();
                for(var seqNumberSet: BarrelMulticastWorker.aheadBuffer.get(downloader).entrySet()){
                    int seqNumber = seqNumberSet.getKey();
                    for(var msgsLeftSet: BarrelMulticastWorker.aheadBuffer.get(downloader).get(seqNumber)
                            .entrySet()){
                        long timeTemp = TimedByteBuffer.TIMEOUT_MS - msgsLeftSet.getValue().timeSinceCreation();
                        if(minWaitTime == -1 || timeTemp < minWaitTime){
                            if(timeTemp < 0) minWaitTime = 0;
                            else minWaitTime = timeTemp;
                        }
                    }
                }
            }
        }

        //check if there are any seqNumbers ahead of the counter (lastSeqNumber)
        synchronized (BarrelMulticastWorker.lastSeqNumber){
            for(var downloaderSet: BarrelMulticastWorker.lastSeqNumber.entrySet()){
                int downloader = downloaderSet.getKey(), seqNumber = downloaderSet.getValue();
                synchronized (BarrelMulticastWorker.downloadersByteBuffers){
                    for(var seqNumberSet: BarrelMulticastWorker.downloadersByteBuffers.get(downloader).entrySet()){
                        if(seqNumberSet.getKey() > seqNumber){
                            long timeTemp = TimedByteBuffer.TIMEOUT_MS - seqNumberSet.getValue().timeSinceCreation();
                            if(minWaitTime == -1 || timeTemp < minWaitTime){
                                if(timeTemp < 0) minWaitTime = 0;
                                else minWaitTime = timeTemp;
                            }
                        }
                    }
                }
            }
        }
        return minWaitTime;
    }

    /**
     * Check if there are packets in the nackAcksQueue with the required parameters
     * @param nack0 first desired parameter of the packet
     * @param nack1 second desired parameter of the packet
     * @return
     */
    public int containsNack(int nack0, int nack1){
        for(int i = 0; i < nackAcksQueue.size(); i++){
            ByteBuffer bb = ByteBuffer.wrap(nackAcksQueue.get(i));
            int barrelId = bb.getInt(), downloaderId = bb.getInt(), seqNumber = bb.getInt(), msgType = bb.getInt();
            if(nack0 == downloaderId && nack1 == seqNumber){
                nackAcksQueue.remove(i);
                return msgType;
            }
        }
        return 0;
    }

    public void run(){
        System.out.println("BarrelMulticastRecovery " + id);
        try (MulticastSocket socket = new MulticastSocket()) {
            long minWaitTime = 0; //time to wait for next out of order packet timeout

            //wait while there are no out of order packets
            while(true){
                synchronized (BarrelMulticastWorker.aheadBuffer){
                    minWaitTime = searchOutOfOrderPackets();
                    while(minWaitTime < 0){
                        BarrelMulticastWorker.aheadBuffer.wait();
                        minWaitTime = searchOutOfOrderPackets();
                    }
                }
                //Wait for the next out of order packet timeout
                Thread.sleep(minWaitTime);

                //Don't start recovery if there is an inter barrel synchronization going on
                synchronized (InterBarrelSynchronizerInserter.syncLock){
                    while(InterBarrelSynchronizerInserter.needSync == 1)
                        InterBarrelSynchronizerInserter.syncLock.wait();
                }

                //list of nacks to be sent to the downloaders
                List<List<Integer>> nacks = new ArrayList<>();

                //get seqNumbers ahead of the counter (lastSeqNumber)
                synchronized (BarrelMulticastWorker.lastSeqNumber){
                    for(var downloaderSet: BarrelMulticastWorker.lastSeqNumber.entrySet()){
                        int downloader = downloaderSet.getKey(), seqNumber = downloaderSet.getValue(),
                            newLastSeqNumber = 0;
                        synchronized (BarrelMulticastWorker.downloadersByteBuffers){
                            for(var seqNumberSet: BarrelMulticastWorker.downloadersByteBuffers.get(downloader).entrySet()){
                                int currentSeqNumber = seqNumberSet.getKey();
                                if(currentSeqNumber > seqNumber){
                                    TimedByteBuffer tbb = seqNumberSet.getValue();
                                    if(tbb.timeSinceCreation() >= TimedByteBuffer.TIMEOUT_MS){
                                        if(newLastSeqNumber == 0 || currentSeqNumber < newLastSeqNumber){
                                            newLastSeqNumber = currentSeqNumber;
                                        }
                                        for(int i = seqNumber + 1; i < currentSeqNumber; i++){
                                            List<Integer> currentPossibleNack = Arrays.asList(downloader, i);
                                            if(!nacks.contains(currentPossibleNack)) nacks.add(currentPossibleNack);
                                        }
                                    }
                                }
                            }
                        }
                        if(newLastSeqNumber != 0) {
                            //update the counters so that the same ack isn't send twice
                            BarrelMulticastWorker.lastSeqNumber.put(downloader, newLastSeqNumber);
                            Barrel.bmw.writeLastSeqNumberFile();
                        }
                    }
                }

                //get buffered packets
                synchronized (BarrelMulticastWorker.aheadBuffer){
                    List<List<Integer>> removeFromAheadBuffer = new ArrayList<>();
                    for(var downloaderSet: BarrelMulticastWorker.aheadBuffer.entrySet()){
                        int downloader = downloaderSet.getKey();
                        for(var seqNumberSet: BarrelMulticastWorker.aheadBuffer.get(downloader).entrySet()){
                            int seqNumber = seqNumberSet.getKey();
                            for(var msgsLeftSet: BarrelMulticastWorker.aheadBuffer.get(downloader).get(seqNumber)
                                    .entrySet()){
                                int msgsLeft = msgsLeftSet.getKey();
                                TimedByteBuffer tbb = msgsLeftSet.getValue();
                                synchronized (BarrelMulticastWorker.lastMsgsLeft){
                                    //if a there is a buffered packet in the current seqNumber of the current
                                    //downloader and it has timed out, register NACK to send to downloader
                                    List<Integer> currentPossibleNack = Arrays.asList(downloader, seqNumber);
                                    if (msgsLeft < BarrelMulticastWorker.lastMsgsLeft.get(downloader).get(seqNumber)
                                        - 1 && tbb.timeSinceCreation() >= TimedByteBuffer.TIMEOUT_MS &&
                                        !nacks.contains(currentPossibleNack)) {

                                        removeFromAheadBuffer.add(currentPossibleNack);
                                        nacks.add(currentPossibleNack);
                                    }
                                }
                            }
                        }
                    }
                    //add necessary nacks to be sent to the downloaders
                    for(var nack: removeFromAheadBuffer){
                        BarrelMulticastWorker.aheadBuffer.get(nack.get(0)).remove(nack.get(1));
                        if(BarrelMulticastWorker.aheadBuffer.get(nack.get(0)).isEmpty())
                            BarrelMulticastWorker.aheadBuffer.remove(nack.get(0));
                    }
                }
                //send nacks (each at max MAX_RETRIES times)
                for(var nack: nacks){
                    int foundNack = 0;
                    for(int i = 0; i < MAX_RETRIES; i++){
                        System.out.println("NACK " + nack.get(0) + " " + nack.get(1));
                        byte[] nackBuffer = new MulticastPacket(id, nack.get(0), nack.get(1), -1, -1).toBytes();
                        //Send the nack packet
                        InetAddress group = InetAddress.getByName(Barrel.MULTICAST_ADDRESS);
                        DatagramPacket packet = new DatagramPacket(nackBuffer, nackBuffer.length, group,
                                DownloaderManager.MULTICAST_PORT);
                        socket.send(packet);

                        //wait at max TIMEOUT_MS ms for a response
                        long sendTime = System.currentTimeMillis();
                        synchronized (nackAcksQueue){
                            do{
                                nackAcksQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                                foundNack = containsNack(nack.get(0), nack.get(1));
                            }
                            while(foundNack == 0 && System.currentTimeMillis() - sendTime < TimedByteBuffer.TIMEOUT_MS);
                        }
                        if(foundNack == 0){
                            System.out.println("NOT FOUND NACK " + nack.get(0) + " " + nack.get(1)
                                    + ", RETRYING... RETRY NUMBER " + (i+1));
                        }
                        else{
                            break;
                        }
                    }


                    byte[] nackBuffer = new MulticastPacket(id, nack.get(0), nack.get(1), -4, -1).toBytes();
                    //Send the nack ack ack packet
                    InetAddress group = InetAddress.getByName(Barrel.MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(nackBuffer, nackBuffer.length, group,
                            DownloaderManager.MULTICAST_PORT);
                    socket.send(packet);

                    System.out.println("Sending NACK ACK ACK " +  id + " " + nack.get(0) + " " + nack.get(1) + " -4");

                    //check if the downloader didn't have the required sequence number buffered,
                    if(foundNack == -3){
                        //start inter barrel synchronization process
                        synchronized (InterBarrelSynchronizerInserter.syncLock){
                            InterBarrelSynchronizerInserter.needSync = 1;
                            InterBarrelSynchronizerInserter.syncLock.notify();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("BarrelMulticastRecovery " + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
