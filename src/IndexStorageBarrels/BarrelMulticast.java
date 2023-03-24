package IndexStorageBarrels;

import classes.MulticastPacket;

import java.net.*;
import java.nio.ByteBuffer;

public class BarrelMulticast implements Runnable{
    private static final String MULTICAST_ADDRESS = "224.0.1.0";
    private static final int MULTICAST_PORT = 5000;
    public int id;
    public Thread t;

    public BarrelMulticast(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }
    public void run(){
        System.out.println("BarrelMulticast " + id);
        try (MulticastSocket socket = new MulticastSocket(MULTICAST_PORT)) {
            // create socket and bind it
            InetAddress mcastaddr = InetAddress.getByName(MULTICAST_ADDRESS);
            InetSocketAddress group = new InetSocketAddress(mcastaddr, MULTICAST_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");

            socket.joinGroup(group, netIf);

            while (true) {
                byte[] packet_buffer = new byte[MulticastPacket.PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(packet_buffer, packet_buffer.length);
                socket.receive(packet);

                //Get packet header bytes
                ByteBuffer bb = ByteBuffer.wrap(packet.getData(), 0, MulticastPacket.HEADER_SIZE);

                //"Unpack" the header bytes
                int downloader_id = bb.getInt(), seq_number = bb.getInt(), msgs_left = bb.getInt(),
                        first_msg = bb.getInt();

                synchronized (Barrel.bPageQueue){
                    Barrel.bPageQueue.add(packet.getData());
                    Barrel.bPageQueue.notify();
                }

            }
        } catch (Exception e) {
            System.out.println("BarrelMulticast " + id + " exception: " + e.getMessage());
        }
    }
}
