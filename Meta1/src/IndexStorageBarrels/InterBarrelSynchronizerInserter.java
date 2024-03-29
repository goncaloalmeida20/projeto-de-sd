package IndexStorageBarrels;

import classes.MulticastPacket;
import classes.Page;
import classes.TimedByteBuffer;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;
import java.util.*;

public class InterBarrelSynchronizerInserter implements Runnable{
    public int id;
    public Thread t;
    public static final List<byte[]> inserterQueue = Collections.synchronizedList(new ArrayList<>());

    public static final Object syncLock = new Object();
    public static int needSync = 0;

    private static final int MAX_RETRIES = 3;

    public InterBarrelSynchronizerInserter(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    /**
     * Check if a packet of the required type exists in the helperQueue
     * @param belowZero type of check to perform (related to the third header field)
     * @param type the required type
     * @return the index of the packet; -1 in case none was found
     */
    public int packetTypeExists(boolean belowZero, int type){
        ByteBuffer bbTemp;
        for(int i = 0; i < inserterQueue.size(); i++){
            bbTemp = ByteBuffer.wrap(inserterQueue.get(i), 0, MulticastPacket.HEADER_SIZE);
            int barrelId = bbTemp.getInt(), senderId = bbTemp.getInt(), msgType = bbTemp.getInt(),
                dlId = bbTemp.getInt(), cSN = bbTemp.getInt();
            if(barrelId == -2) continue;
            if(belowZero && type == -2 && msgType == type){
                return i;
            }
            if(belowZero && type == -5 && msgType == type){
                return i;
            }
            if(!belowZero && msgType >= 0){
                return i;
            }
        }
        return -1;
    }

    public void run(){
        System.out.println("InterBarrelSynchronizerInserter " + id);
        try (MulticastSocket socket = new MulticastSocket()) {
            while(true){
                synchronized (syncLock){
                    while(needSync == 0){
                        syncLock.wait();
                    }
                }
                synchronized (inserterQueue){
                    inserterQueue.clear();
                }
                byte[] queuePacket = null;
                //Ask the other barrels for the number of downloaders
                for(int r = 0; r < MAX_RETRIES; r++){
                    byte[] packetBuffer =
                            new MulticastPacket(-2, id, -4, -2, -2).toBytes();

                    //send the packet
                    InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                            Barrel.SYNC_MULTICAST_PORT);
                    socket.send(packet);

                    //check if the response packet exists in the inserterQueue
                    queuePacket = null;
                    synchronized (inserterQueue){
                        int packetIndex = packetTypeExists(true, -5);
                        long initialTime = System.currentTimeMillis();
                        while(packetIndex == -1 &&
                                System.currentTimeMillis() - initialTime < TimedByteBuffer.TIMEOUT_MS){
                            inserterQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                            packetIndex = packetTypeExists(true, -5);
                        }
                        if(packetIndex != -1) queuePacket = inserterQueue.remove(packetIndex);
                    }
                    if(queuePacket != null){
                        //get packet bytes
                        ByteBuffer bb = ByteBuffer.wrap(queuePacket);

                        //get the header bytes
                        int barrelId = bb.getInt(), senderId = bb.getInt(), msgType = bb.getInt(),
                                downloaderCount = bb.getInt();
                        //create indexes for new downloaders
                        synchronized (BarrelMulticastWorker.lastSeqNumber){
                            if(downloaderCount > BarrelMulticastWorker.lastSeqNumber.size()){
                                for(int i = 0; i < downloaderCount; i++){
                                    if(!BarrelMulticastWorker.lastSeqNumber.containsKey(i + 1)){
                                        BarrelMulticastWorker.lastSeqNumber.put(i + 1, 0);
                                    }
                                }
                            }
                            Barrel.bmw.writeLastSeqNumberFile();
                        }
                        break;
                    }
                    else{
                        System.out.println("PACKET NOT RECEIVED, RETRYING... (RETRY NUMBER " + (r+1) + ")");
                    }
                }
                if(queuePacket == null){
                    System.out.println("No packets received\nAssuming this barrel is the only one running");
                    //End synchronization process
                    synchronized (syncLock){
                        needSync = 0;
                        syncLock.notifyAll();
                    }
                    continue;
                }

                boolean errorOcurred = false;

                //Recover lost messages from each of the downloaders by communicating with the other barrels
                Set<Map.Entry<Integer, Integer>> entries;
                synchronized (BarrelMulticastWorker.lastSeqNumber){
                     entries = BarrelMulticastWorker.lastSeqNumber.entrySet();
                }
                for(var entry: entries){
                    int downloaderId = entry.getKey(), currentSeqNumber = entry.getValue();

                    byte[] packetBuffer =
                            new MulticastPacket(-1, id, -1, downloaderId, currentSeqNumber).toBytes();

                    //send packet asking for other barrels to compare the local last sequence number of the current
                    //downloader with theirs
                    InetAddress group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                            Barrel.SYNC_MULTICAST_PORT);
                    socket.send(packet);

                    //wait for response
                    synchronized (inserterQueue){
                        int packetIndex = packetTypeExists(true, -2);
                        long initialTime = System.currentTimeMillis();
                        while(packetIndex == -1 &&
                                System.currentTimeMillis() - initialTime < TimedByteBuffer.TIMEOUT_MS){
                            inserterQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                            packetIndex = packetTypeExists(true, -2);
                        }
                        if(packetIndex != -1) queuePacket = inserterQueue.remove(packetIndex);
                        else{
                            System.out.println("No synchronization packets received, assuming this barrel " +
                                    "has the correct packets from downloader " + downloaderId);
                            continue;
                        }
                    }

                    //get packet bytes
                    ByteBuffer bb = ByteBuffer.wrap(queuePacket);

                    //get the header bytes
                    int barrelId = bb.getInt(), senderId = bb.getInt();

                    packetBuffer =
                            new MulticastPacket(senderId, id, -3, downloaderId, currentSeqNumber).toBytes();

                    //start recovering messages from the current downloader, sent by the barrel that noticed a
                    //significant difference in packet numbers
                    group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                    packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                            Barrel.SYNC_MULTICAST_PORT);
                    socket.send(packet);

                    while(true) {
                        ByteBuffer messageBB = null;

                        int msgsLeft = -2;
                        int seqNumber = -1;
                        while(msgsLeft != 0){
                            //check if the next packet has arrived
                            synchronized (inserterQueue){
                                int packetIndex = packetTypeExists(false, 0);
                                long initialTime = System.currentTimeMillis();
                                while(packetIndex == -1 &&
                                        System.currentTimeMillis() - initialTime < TimedByteBuffer.TIMEOUT_MS){
                                    inserterQueue.wait(TimedByteBuffer.TIMEOUT_MS);
                                    packetIndex = packetTypeExists(false, 0);
                                }
                                if(packetIndex != -1) queuePacket = inserterQueue.remove(packetIndex);
                                else{
                                    errorOcurred = true;
                                    break;
                                }
                            }

                            //get packet bytes
                            ByteBuffer tempbb = ByteBuffer.wrap(queuePacket);

                            //get the header bytes
                            int tempBarrelId = tempbb.getInt(), tempSenderid = tempbb.getInt();
                            seqNumber = tempbb.getInt();
                            msgsLeft = tempbb.getInt();
                            int firstMsg = tempbb.getInt();

                            if(msgsLeft == -1){
                                //end of transmission
                                break;
                            }

                            //add bytes to the current buffer
                            if(messageBB == null){
                                messageBB = ByteBuffer.allocate((msgsLeft + 1) * MulticastPacket.MSG_BYTES_SIZE);
                            }
                            messageBB.put(tempbb);

                            //send ack
                            packetBuffer = new MulticastPacket(tempSenderid, id, seqNumber, msgsLeft, -1).toBytes();

                            group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                            packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                                    Barrel.SYNC_MULTICAST_PORT);
                            socket.send(packet);
                        }

                        if(msgsLeft == 0){
                            //increment seqNumber
                            synchronized (BarrelMulticastWorker.lastSeqNumber){
                                BarrelMulticastWorker.lastSeqNumber.put(downloaderId, seqNumber);
                                Barrel.bmw.writeLastSeqNumberFile();
                            }
                            System.out.println("Recovered " + downloaderId + " " + seqNumber);
                            byte[] messageBytes = messageBB.array();
                            int newLength; //length without trailing zeros
                            for(newLength = messageBytes.length - 1; newLength >= 0 && messageBytes[newLength] == 0; newLength--);

                            String message = new String(messageBytes, 0, newLength + 1);

                            System.out.println("Received from Barrel " + senderId + " seqNumber " + seqNumber
                                    + " " + message);

                            //add page to the local database
                            Page receivedPage = new Page(message);
                            Barrel.bdb.insertPage(receivedPage, downloaderId, seqNumber);
                        }
                        else if(msgsLeft == -1){
                            //send ack of end of transmission
                            packetBuffer = new MulticastPacket(senderId, id, seqNumber, msgsLeft, -1).toBytes();

                            group = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
                            packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                                    Barrel.SYNC_MULTICAST_PORT);
                            socket.send(packet);
                            break;
                        }
                        else{
                            errorOcurred = true;
                            break;
                        }
                    }
                    if(errorOcurred) break;

                }
                if(!errorOcurred){
                    //Synchronization finished successfully, reactivating the waiting threads
                    synchronized (syncLock){
                        needSync = 0;
                        syncLock.notifyAll();
                    }
                    System.out.println("Synchronization finished");
                }
            }
        } catch (Exception e) {
            System.out.println("InterBarrelSynchronizerInserter " + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
