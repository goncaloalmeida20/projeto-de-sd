package IndexStorageBarrels;

import classes.MulticastPacket;
import classes.Page;
import classes.TimedByteBuffer;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.*;

public class BarrelMulticastWorker implements Runnable{
    public static final List<byte[]> msgPacketQueue = Collections.synchronizedList(new ArrayList<>());
    public static final Map<Integer, Map<Integer, TimedByteBuffer>> downloadersByteBuffers = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, Integer> lastSeqNumber = Collections.synchronizedMap(new HashMap<>());
    public static final Map<Integer, Map<Integer, Integer>> lastMsgsLeft = Collections.synchronizedMap(new HashMap<>());

    public static final Map<Integer, Map<Integer, Map<Integer, TimedByteBuffer>>> aheadBuffer
            = Collections.synchronizedMap(new HashMap<>());


    public Thread t;
    public int id;
    public BarrelMulticastWorker(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("BarrelMulticastWorker " + id);

        try {
            while (true) {
                byte[] packet;

                //wait for packets to arrive
                synchronized (msgPacketQueue){
                    while(msgPacketQueue.size() == 0){
                        msgPacketQueue.wait();
                    }

                    packet = msgPacketQueue.remove(0);
                }

                //get packet bytes
                ByteBuffer bb = ByteBuffer.wrap(packet);

                //get the header bytes
                int barrelId = bb.getInt(), downloaderId = bb.getInt(), seqNumber = bb.getInt(), msgsLeft = bb.getInt(),
                        firstMsg = bb.getInt();

                System.out.println("Analyzing " + barrelId + " " + downloaderId + " " + seqNumber + " " + msgsLeft +
                        " " + firstMsg);

                boolean aheadSeqNumber = false, aheadMsgsLeft = false;

                //check if the sequence number is the expected one
                synchronized (lastSeqNumber){
                    if(lastSeqNumber.containsKey(downloaderId)){
                        int expectedSeqNumber = lastSeqNumber.get(downloaderId) + 1;
                        //if seqNumber is lower than expected, ignore packet
                        if(seqNumber < expectedSeqNumber - 1) continue;
                        //if it is higher than expected, notify recovery thread
                        if(seqNumber > expectedSeqNumber){
                            aheadSeqNumber = true;
                        }
                    }
                    else if(seqNumber != 1){
                        lastSeqNumber.put(downloaderId, 0);
                        aheadSeqNumber = true;
                    }
                }


                //System.out.println("HELLO1");
                //check if the order of the message packets (msgsLeft) of the current seqNumber is right
                synchronized (lastMsgsLeft){
                    if(lastMsgsLeft.containsKey(downloaderId)){
                        if(lastMsgsLeft.get(downloaderId).containsKey(seqNumber)){
                            int expectedMsgsLeft = lastMsgsLeft.get(downloaderId).get(seqNumber) - 1;
                            //if msgsLeft is higher than expected, ignore packet
                            if(msgsLeft > expectedMsgsLeft) continue;
                            //if it is lower than expected, notify recovery thread
                            if(msgsLeft < expectedMsgsLeft){
                                aheadMsgsLeft = true;
                            }
                        }else if(msgsLeft != firstMsg){
                            lastMsgsLeft.get(downloaderId).put(seqNumber, firstMsg+1);
                            aheadMsgsLeft = true;
                        }
                    } else{
                        lastMsgsLeft.put(downloaderId, new HashMap<>());
                        if(msgsLeft != firstMsg){
                            lastMsgsLeft.get(downloaderId).put(seqNumber, firstMsg+1);
                            aheadMsgsLeft = true;
                        }
                    }
                }
                //System.out.println("HELLO2");

                //if it's the first msg from the downloader, create the hashmap
                synchronized (downloadersByteBuffers){
                    if(!downloadersByteBuffers.containsKey(downloaderId)){
                        downloadersByteBuffers.put(downloaderId, new HashMap<>());
                    }
                    if(!downloadersByteBuffers.get(downloaderId).containsKey(seqNumber)){
                        downloadersByteBuffers.get(downloaderId).put(seqNumber, new TimedByteBuffer());
                    }
                }

                //System.out.println("HELLO3 " + aheadSeqNumber);
                //if the seqNumber order is right, update the last right seqNumber
                synchronized (lastSeqNumber){
                    if(!aheadSeqNumber) lastSeqNumber.put(downloaderId, seqNumber);
                }

                //System.out.println("AAAA " + BarrelMulticastWorker.lastSeqNumber.get(downloaderId));

                synchronized (aheadBuffer){
                    if(aheadMsgsLeft){
                        //buffer out of order bytes
                        synchronized (aheadBuffer){
                            if(!aheadBuffer.containsKey(downloaderId))
                                aheadBuffer.put(downloaderId, new HashMap<>());
                            if(!aheadBuffer.get(downloaderId).containsKey(seqNumber))
                                aheadBuffer.get(downloaderId).put(seqNumber, new HashMap<>());
                            aheadBuffer.get(downloaderId).get(seqNumber)
                                    .put(msgsLeft, new TimedByteBuffer(bb));
                            aheadBuffer.notify();
                        }
                        continue;
                    }
                }

                synchronized (downloadersByteBuffers){
                    //if it's the first packet of the current message, allocate a buffer
                    if(msgsLeft == firstMsg){
                        downloadersByteBuffers.get(downloaderId).put(seqNumber, new TimedByteBuffer(
                                ByteBuffer.allocate((msgsLeft + 1) * MulticastPacket.MSG_BYTES_SIZE)));
                    }
                    //append the bytes to the current sequence number buffer
                    downloadersByteBuffers.get(downloaderId).get(seqNumber).byteBuffer.put(bb);
                    synchronized (aheadBuffer){
                        if(aheadBuffer.containsKey(downloaderId)
                                && aheadBuffer.get(downloaderId).containsKey(seqNumber)){
                            while(aheadBuffer.get(downloaderId).get(seqNumber).containsKey(msgsLeft-1)){
                                msgsLeft--;
                                downloadersByteBuffers.get(downloaderId).get(seqNumber).byteBuffer.put(
                                        aheadBuffer.get(downloaderId).get(seqNumber).remove(msgsLeft).byteBuffer);
                            }
                        }
                    }
                }

                synchronized (lastMsgsLeft){
                    //update the last right msgsLeft
                    lastMsgsLeft.get(downloaderId).put(seqNumber, msgsLeft);
                }



                //check if the current sequence number message is complete
                if(msgsLeft == 0){
                    byte[] msgBytes;
                    synchronized (downloadersByteBuffers){
                        msgBytes = downloadersByteBuffers.get(downloaderId).get(seqNumber).byteBuffer.array();
                    }
                    int newLength; //length without trailing zeros
                    for(newLength = msgBytes.length - 1; newLength >= 0 && msgBytes[newLength] == 0; newLength--);

                    String message = new String(msgBytes, 0, newLength + 1);
                    System.out.println("Received from Downloader " + downloaderId + " seqNumber " + seqNumber
                            + " " + message);

                    Page receivedPage = new Page(message);
                }

                if(aheadSeqNumber) synchronized(aheadBuffer){ aheadBuffer.notify();}
            }
        } catch (Exception e) {
            System.out.println("BarrelMulticastWorker " + id + " exception: " + e.getMessage());
        }
    }
}
