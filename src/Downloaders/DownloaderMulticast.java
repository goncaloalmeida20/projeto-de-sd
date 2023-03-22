package Downloaders;

import classes.MulticastPacket;
import classes.Page;

import java.io.ByteArrayInputStream;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.ByteBuffer;

public class DownloaderMulticast implements Runnable{
    public Thread t;
    public int id;
    public DownloaderMulticast(int id){
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
                    System.out.println("DownloaderMulticast " + id + " sending page " + currentPage.url);
                    DownloaderManager.pageQueue.notify();
                }

                //Convert the page to the multicast udp protocol string
                byte[] pageBytes = currentPage.multicastString().getBytes(),
                        msgBytes = new byte[MulticastPacket.MSG_BYTES_SIZE],
                        packet_buffer;

                //Split the message so that each part doesn't exceed the packet size
                ByteArrayInputStream bis = new ByteArrayInputStream(pageBytes);
                DownloaderManager.seq_number++;

                //length - 1 so that if length = MSG_BYTES_SIZE, only 1 packet is sent
                for (int i = (pageBytes.length - 1) / MulticastPacket.MSG_BYTES_SIZE; i >= 0; i--) {
                    if (bis.read(msgBytes, 0, MulticastPacket.MSG_BYTES_SIZE) < 0) {
                        throw new Exception("MulticastPacket bytes ended");
                    }
                    MulticastPacket mp = new MulticastPacket(id, DownloaderManager.seq_number, i, msgBytes);
                    packet_buffer = mp.toBytes();
                    ByteBuffer bb = ByteBuffer.wrap(packet_buffer);

                    //Send the packet
                    InetAddress group = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
                    DatagramPacket packet = new DatagramPacket(packet_buffer, packet_buffer.length, group,
                    DownloaderManager.MULTICAST_PORT);
                    socket.send(packet);
                }
            }
        } catch (Exception e) {
            System.out.println("DownloaderMulticast " + id + " exception: " + e.getMessage());
        }
    }
}
