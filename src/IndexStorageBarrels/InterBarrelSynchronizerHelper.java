package IndexStorageBarrels;

import classes.MulticastPacket;
import classes.Page;
import classes.TimedByteBuffer;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.*;

public class InterBarrelSynchronizerHelper implements Runnable{
    public int id;
    public Thread t;
    public static final List<byte[]> helperQueue = Collections.synchronizedList(new ArrayList<>());

    private static final int MAX_RETRIES = 5;

    public InterBarrelSynchronizerHelper(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    /**
     * Check if a packet of the required type exists in the helperQueue
     * @param belowZero type of check to perform (related to the third header field)
     * @return the index of the required packet; -1 in case none was found
     */
    public int packetTypeExists(boolean belowZero){
        ByteBuffer bbTemp;
        for(int i = 0; i < helperQueue.size(); i++){
            bbTemp = ByteBuffer.wrap(helperQueue.get(i), 0, MulticastPacket.HEADER_SIZE);
            int barrelId = bbTemp.getInt(), senderId = bbTemp.getInt(), msgType = bbTemp.getInt();
            if(belowZero && msgType < 0){
                return i;
            }
            if(!belowZero && msgType >= 0){
                return i;
            }
        }
        return -1;
    }

    public void run(){
        System.out.println("InterBarrelSynchronizerHelper " + id);
        try(MulticastSocket socket = new MulticastSocket()) {
            while (true) {
                byte[] queuePacket;
                //wait for packets to arrive
                synchronized (helperQueue){
                    int packetIndex = packetTypeExists(true);
                    while(packetIndex == -1){
                        helperQueue.wait();
                        packetIndex = packetTypeExists(true);
                    }
                    queuePacket = helperQueue.remove(packetIndex);
                }

                //get packet bytes
                ByteBuffer bb = ByteBuffer.wrap(queuePacket);

                //get the header bytes
                int barrelId = bb.getInt(), senderId = bb.getInt(), msgType = bb.getInt(), downloaderId = bb.getInt(),
                        seqNumber = bb.getInt();

                if(barrelId == -2){
                    //Send number of known downloaders
                    int dlNum;
                    synchronized (BarrelMulticastWorker.lastSeqNumber){
                        dlNum = BarrelMulticastWorker.lastSeqNumber.size();
                    }
                    byte[] packetBuffer = new MulticastPacket(
                            senderId, id, -5, dlNum, -2).toBytes();
                    InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                            Barrel.SYNC_MULTICAST_PORT);
                    socket.send(packet);
                    continue;
                }
                if(msgType == -1){
                    //Offer help in recovering lost packets (code = -2)
                    int localDownloaderSeqNumber = -1;
                    synchronized (BarrelMulticastWorker.lastSeqNumber){
                        //get the current seqNumber of the current downloaderId on this barrel
                        if(BarrelMulticastWorker.lastSeqNumber.containsKey(downloaderId))
                            localDownloaderSeqNumber = BarrelMulticastWorker.lastSeqNumber.get(downloaderId);

                    }

                    //check if this barrel contains information about the required downloader and the difference
                    //in sequence numbers is significant
                    if(localDownloaderSeqNumber == -1 ||
                            localDownloaderSeqNumber - seqNumber < Barrel.SEQ_NUMBER_DIFF_TOLERANCE){
                        continue;
                    }

                    //send the packet
                    byte[] packetBuffer = new MulticastPacket(
                            senderId, id, -2, downloaderId, localDownloaderSeqNumber).toBytes();
                    InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                            Barrel.SYNC_MULTICAST_PORT);
                    socket.send(packet);
                    continue;
                }

                //msgType is -3
                Map<Page, Integer> pagesToSend = Barrel.bdb.sendDownloaderPages(downloaderId, seqNumber);
                int currentSeqNumber = -6;

                //get the pages needed by the other barrel and sort them by sequence number
                List<Map.Entry<Page, Integer>> keys = new ArrayList<>(pagesToSend.entrySet());
                keys.sort(Map.Entry.comparingByValue());
                for(var entry: keys){
                    Page currentPage = entry.getKey();
                    currentSeqNumber = entry.getValue();

                    byte[] pageBytes = currentPage.multicastString().getBytes(), packetBuffer;

                    //Split the message so that each part doesn't exceed the packet size
                    ByteArrayInputStream bis = new ByteArrayInputStream(pageBytes);

                    //length - 1 so that if length = MSG_BYTES_SIZE, only 1 packet is sent
                    int initialMsgSeqNumber = (pageBytes.length - 1) / MulticastPacket.MSG_BYTES_SIZE;
                    for (int i = initialMsgSeqNumber; i >= 0; i--) {
                        byte[] msgBytes = new byte[MulticastPacket.MSG_BYTES_SIZE];
                        if (bis.read(msgBytes, 0, MulticastPacket.MSG_BYTES_SIZE) < 0) {
                            throw new Exception("MulticastPacket bytes ended");
                        }
                        MulticastPacket mp = new MulticastPacket(senderId, id, currentSeqNumber, i,
                                initialMsgSeqNumber, msgBytes);

                        packetBuffer = mp.toBytes();

                        //Send each packet at max MAX_RETRIES times and wait for ACK
                        byte[] ackPacket = null;
                        for(int j = 0; ackPacket == null && j < MAX_RETRIES; j++){
                            //Send the packet
                            InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                            DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group, Barrel.
                                    SYNC_MULTICAST_PORT);
                            socket.send(packet);

                            //wait for packets to arrive at max TIMEOUT_MS seconds
                            synchronized (helperQueue){
                                int packetIndex = packetTypeExists(false);
                                long initialTime = System.currentTimeMillis();
                                while(packetIndex == -1 &&
                                        System.currentTimeMillis() - initialTime < TimedByteBuffer.TIMEOUT_MS){
                                    helperQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                                    packetIndex = packetTypeExists(false);
                                }
                                if(packetIndex != -1) ackPacket = helperQueue.remove(packetIndex);
                                else System.out.println("PACKET NOT RECEIVED, RETRYING... (RETRY NUMBER " + (j+1) + ")");
                            }
                        }
                        if(ackPacket == null){
                            System.out.println("PACKET NOT RECEIVED");
                            break;
                        }
                        else{
                            ByteBuffer bbTemp = ByteBuffer.wrap(ackPacket);

                            int tempBarrelId = bbTemp.getInt(), tempSenderId = bbTemp.getInt(), tempSeqNumber = bbTemp.getInt(),
                                    tempMsgsLeft = bbTemp.getInt(), tempAck = bbTemp.getInt();
                            System.out.println("RECEIVED ACK " + (tempSeqNumber == currentSeqNumber &&
                                    tempMsgsLeft == i && tempAck == -1));
                            /*bbTemp.rewind();
                            for (int k = 0; k < 5; k++){
                                System.out.print(bbTemp.getInt() + " ");
                            }
                            System.out.println();*/
                        }
                    }
                }
                if(currentSeqNumber == -6) continue;

                //Send a packet signaling the end of the packet transmission
                byte[] packetBuffer =
                        new MulticastPacket(senderId, id, currentSeqNumber, -1, -9999).toBytes();

                //Wait for ACK at most TIMEOUT_MS ms, at most MAX_RETRIES times
                byte[] ackPacket = null;
                for(int j = 0; ackPacket == null && j < MAX_RETRIES; j++){
                    //Send the packet
                    InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group, Barrel.
                            SYNC_MULTICAST_PORT);
                    socket.send(packet);

                    //wait for packets to arrive at max TIMEOUT_MS seconds
                    synchronized (helperQueue){
                        int packetIndex = packetTypeExists(false);
                        long initialTime = System.currentTimeMillis();
                        while(packetIndex == -1 &&
                                System.currentTimeMillis() - initialTime < TimedByteBuffer.TIMEOUT_MS){
                            helperQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                            packetIndex = packetTypeExists(false);
                        }
                        if(packetIndex != -1) ackPacket = helperQueue.remove(packetIndex);
                        else System.out.println("PACKET NOT RECEIVED, RETRYING... (RETRY NUMBER " + (j+1) + ")");
                    }
                }
                if(ackPacket == null){
                    System.out.println("PACKET NOT RECEIVED");
                    break;
                }
                else{
                    ByteBuffer bbTemp = ByteBuffer.wrap(ackPacket);

                    int tempBarrelId = bbTemp.getInt(), tempSenderId = bbTemp.getInt(), tempSeqNumber = bbTemp.getInt(),
                            tempMsgsLeft = bbTemp.getInt(), tempAck = bbTemp.getInt();
                    System.out.println("RECEIVED ACK " + (tempSeqNumber == currentSeqNumber &&
                            tempMsgsLeft == -1 && tempAck == -1));
                    /*bbTemp.rewind();
                    for (int k = 0; k < 5; k++){
                        System.out.print(bbTemp.getInt() + " ");
                    }
                    System.out.println();*/
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("InterBarrelSynchronizerHelper " + id + " exception: " + e.getMessage());
        }
    }
}
