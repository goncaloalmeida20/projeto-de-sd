package IndexStorageBarrels;

import classes.MulticastPacket;

import java.net.*;
import java.nio.ByteBuffer;

public class BarrelMulticastReceiver implements Runnable{
    public int id;
    public Thread t;

    public BarrelMulticastReceiver(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("BarrelMulticastReceiver " + id);

        try (MulticastSocket socket = new MulticastSocket(Barrel.MULTICAST_PORT)) {
            // create socket and bind it
            InetAddress mcastaddr = InetAddress.getByName(Barrel.MULTICAST_ADDRESS);
            InetSocketAddress group = new InetSocketAddress(mcastaddr, Barrel.MULTICAST_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");

            // join group
            socket.joinGroup(group, netIf);

            while (true) {
                //receive multicast packet
                byte[] packet_buffer = new byte[MulticastPacket.PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(packet_buffer, packet_buffer.length);
                socket.receive(packet);

                byte[] packetBytes = packet.getData();

                //Get packet header bytes
                ByteBuffer headerBytes = ByteBuffer.wrap(packetBytes, 0, MulticastPacket.HEADER_SIZE);

                //get the header bytes
                int barrelId = headerBytes.getInt(), downloaderId = headerBytes.getInt(),
                        seqNumber = headerBytes.getInt(), messageType = headerBytes.getInt();

                //System.out.println("Received " + barrelId + " " + downloaderId + " " + seqNumber + " " + messageType);

                if(barrelId > 0 && barrelId != id) continue;

                //check if it's a NACK ACK (acknowledge of the NACK message) or a regular message
                if(messageType < 0){
                    synchronized (BarrelMulticastRecovery.nackAcksQueue){
                        BarrelMulticastRecovery.nackAcksQueue.add(packetBytes);
                        BarrelMulticastRecovery.nackAcksQueue.notify();
                    }
                }
                else{
                    synchronized (BarrelMulticastWorker.msgPacketQueue){
                        BarrelMulticastWorker.msgPacketQueue.add(packetBytes);
                        BarrelMulticastWorker.msgPacketQueue.notify();
                    }
                }
            }
        }
        catch(Exception e){
            System.out.println("BarrelMulticastReceiver" + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
