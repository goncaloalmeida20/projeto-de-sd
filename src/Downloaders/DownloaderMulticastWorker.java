package Downloaders;

import classes.MulticastPacket;
import classes.Page;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.ArrayList;
import java.util.List;

public class DownloaderMulticastWorker implements Runnable{
    public Thread t;
    public int id;
    public DownloaderMulticastWorker(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        try (MulticastSocket socket = new MulticastSocket()) {
            socket.setTimeToLive(1);
            while (true) {
                //Wait for the next page to send and get it
                Page currentPage;
                synchronized (DownloaderManager.pageQueue) {
                    while (DownloaderManager.pageQueue.size() == 0) {
                        DownloaderManager.pageQueue.wait();
                    }
                    currentPage = DownloaderManager.pageQueue.remove(0);
                    System.out.println("DownloaderMulticastWorker " + id + " sending page " + currentPage.url);
                    //DownloaderManager.pageQueue.notify();
                }

                //Convert the page to the multicast udp protocol string
                byte[] pageBytes = currentPage.multicastString().getBytes(), packetBuffer;
                System.out.println(currentPage.multicastString());
                //Split the message so that each part doesn't exceed the packet size
                ByteArrayInputStream bis = new ByteArrayInputStream(pageBytes);
                DownloaderManager.seqNumber++;

                //List<byte[]> testBuffers = new ArrayList<>();

                //length - 1 so that if length = MSG_BYTES_SIZE, only 1 packet is sent
                int initialMsgSeqNumber = (pageBytes.length - 1) / MulticastPacket.MSG_BYTES_SIZE;
                for (int i = initialMsgSeqNumber; i >= 0; i--) {
                    byte[] msgBytes = new byte[MulticastPacket.MSG_BYTES_SIZE];
                    if (bis.read(msgBytes, 0, MulticastPacket.MSG_BYTES_SIZE) < 0) {
                        throw new Exception("MulticastPacket bytes ended");
                    }
                    MulticastPacket mp = new MulticastPacket(-1, id, DownloaderManager.seqNumber, i, initialMsgSeqNumber,
                            msgBytes);

                    packetBuffer = mp.toBytes();
                    //System.out.println(new String(msgBytes));
                    //Send the packet
                    InetAddress group = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length, group,
                    DownloaderManager.MULTICAST_PORT);
                    System.out.println("############################# " + currentPage.url);
                    //if(DownloaderManager.seqNumber % 2 != 0 && i % 2 != 0)
                        socket.send(packet);
                    //else if(DownloaderManager.seqNumber % 2 == 0)
                    //    socket.send(packet);
                    //else
                    //    testBuffers.add(packetBuffer);
                }
                /*for (var v: testBuffers){
                    InetAddress group = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(v, v.length, group,
                            DownloaderManager.MULTICAST_PORT);
                    socket.send(packet);
                }*/
                synchronized (DownloaderManager.pageBuffer){
                    DownloaderManager.pageBuffer.put(DownloaderManager.seqNumber, currentPage);
                }

            }
        } catch (Exception e) {
            System.out.println("DownloaderMulticastWorker " + id + " exception: " + e.getMessage());
        }
    }
}
