package Downloaders;

import classes.MulticastPacket;
import classes.TimedByteBuffer;

import java.net.*;
import java.nio.ByteBuffer;

public class DownloaderMulticastReceiver implements Runnable{
    public int id;
    public Thread t;
    public DownloaderMulticastReceiver(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("DownloaderMulticastReceiver " + id);

        try (MulticastSocket socket = new MulticastSocket(DownloaderManager.MULTICAST_PORT)) {
            // create socket and bind it
            InetAddress mcastaddr = InetAddress.getByName(DownloaderManager.MULTICAST_ADDRESS);
            InetSocketAddress group = new InetSocketAddress(mcastaddr, DownloaderManager.MULTICAST_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");

            // join group
            socket.joinGroup(group, netIf);

            while (true) {
                //receive multicast packet
                byte[] packetBuffer = new byte[MulticastPacket.PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(packetBuffer, packetBuffer.length);
                socket.receive(packet);

                byte[] packetBytes = packet.getData();

                //Get packet header bytes
                ByteBuffer bb = ByteBuffer.wrap(packetBytes);

                //get the header bytes
                int barrelId = bb.getInt(), downloaderId = bb.getInt(), seqNumber = bb.getInt(), msgType = bb.getInt(),
                        firstMsg = bb.getInt();

                if(downloaderId != id || barrelId < 0) continue;

                if(msgType == -1){
                    synchronized (DownloaderMulticastRecovery.nackPackets){
                        DownloaderMulticastRecovery.nackPackets.add(new TimedByteBuffer(ByteBuffer.wrap(packetBytes),
                                System.currentTimeMillis()));
                        DownloaderMulticastRecovery.nackPackets.notify();
                    }
                }
                else if(msgType == -4){
                    synchronized (DownloaderMulticastRecovery.nackackacks){
                        DownloaderMulticastRecovery.nackackacks.add(new TimedByteBuffer(ByteBuffer.wrap(packetBytes),
                                System.currentTimeMillis()));
                        DownloaderMulticastRecovery.nackackacks.notify();
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("DownloaderMulticastReceiver" + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
