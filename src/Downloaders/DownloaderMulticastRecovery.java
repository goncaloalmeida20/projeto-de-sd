package Downloaders;

import classes.MulticastPacket;
import classes.Page;
import classes.TimedByteBuffer;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

public class DownloaderMulticastRecovery implements Runnable{
    public int id;
    public Thread t;
    private static final int MAX_RETRIES = 5;
    public static final List<TimedByteBuffer> nackPackets = Collections.synchronizedList(new ArrayList<>());
    public static final List<TimedByteBuffer> nackackacks = Collections.synchronizedList(new ArrayList<>());
    public DownloaderMulticastRecovery(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    private TimedByteBuffer containsAck(int barrelId, int downloaderId, int seqNumber){
        for(var tbb: nackackacks){
            int tempBarrelId = tbb.byteBuffer.getInt(), tempDownloaderId = tbb.byteBuffer.getInt(),
                    tempSeqNumber = tbb.byteBuffer.getInt(), msgType = tbb.byteBuffer.getInt();
            tbb.byteBuffer.rewind();
            if(tempBarrelId == barrelId && tempDownloaderId == downloaderId && tempSeqNumber == seqNumber){
                return tbb;
            }
        }
        return null;
    }

    public void run(){
        System.out.println("DownloaderMulticastRecovery " + id);

        try (MulticastSocket socket = new MulticastSocket()) {
            while (true) {
                ByteBuffer bb;
                synchronized (nackPackets){
                    while(nackPackets.size() == 0){
                        nackPackets.wait();
                    }
                    bb = nackPackets.remove(0).byteBuffer;
                }

                //get the header bytes
                int barrelId = bb.getInt(), downloaderId = bb.getInt(),
                        seqNumber = bb.getInt(), messageType = bb.getInt();

                int msgType;
                Page recoveredPage;
                synchronized (DownloaderManager.pageBuffer){
                    recoveredPage = DownloaderManager.pageBuffer.get(seqNumber);
                    if(recoveredPage == null){
                        msgType = -3;
                    }
                    else{
                        msgType = -2;
                    }
                }
                byte[] packetBuffer = new MulticastPacket(barrelId, id, seqNumber, msgType, -1).toBytes();
                System.out.println("Sending NACK ACK " + barrelId + " " + id + " " + seqNumber + " " + msgType);
                InetAddress group = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
                DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                        DownloaderManager.MULTICAST_PORT);
                socket.send(packet);


                TimedByteBuffer tbb;
                synchronized (nackackacks){
                    tbb = containsAck(barrelId, downloaderId, seqNumber);
                    for(int i = 0; tbb == null && i < MAX_RETRIES; i++){
                        nackackacks.wait(TimedByteBuffer.TIMEOUT_MS);
                        tbb = containsAck(barrelId, downloaderId, seqNumber);
                    }
                }
                if(tbb == null){
                    System.out.println("Lost NACK ACK ACK");
                }
                else{
                    int tempBarrelId = tbb.byteBuffer.getInt(), tempDownloaderId = tbb.byteBuffer.getInt(),
                            tempSeqNumber = tbb.byteBuffer.getInt(), tempMsgType = tbb.byteBuffer.getInt();
                    System.out.println("Received NACK ACK ACK " + tempBarrelId + " " + tempDownloaderId
                            + " " + tempSeqNumber + " " + tempMsgType);
                    if(recoveredPage != null){

                        synchronized (DownloaderManager.pageQueue){
                            synchronized (DownloaderManager.recoveredPages){
                                if(!DownloaderManager.pageQueue.contains(recoveredPage)
                                && !DownloaderManager.recoveredPages.containsKey(recoveredPage)){
                                    DownloaderManager.pageQueue.add(recoveredPage);
                                    DownloaderManager.recoveredPages.put(recoveredPage, System.currentTimeMillis());
                                    DownloaderManager.pageQueue.notify();
                                    System.out.println("Readding page " + recoveredPage.url);
                                }
                            }
                        }
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("DownloaderMulticastRecovery" + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
