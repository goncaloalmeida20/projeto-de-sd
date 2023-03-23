package IndexStorageBarrels;

import classes.MulticastPacket;

import java.net.*;
import java.nio.ByteBuffer;

public class BarrelMulticastWorker implements Runnable{
    private static final String MULTICAST_ADDRESS = "224.0.1.0";
    private static final int MULTICAST_PORT = 5000;
    public Thread t;
    public int id;
    public BarrelMulticastWorker(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("BarrelMulticastWorker " + id);
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

                //Get packet bytes
                ByteBuffer bb = ByteBuffer.wrap(packet.getData());

                //"Unpack" the bytes
                int downloader_id = bb.getInt(), seq_number = bb.getInt(), msgs_left = bb.getInt(),
                        first_msg = bb.getInt();

                //Allocate a buffer
                int msgSize = (msgs_left + 1) * MulticastPacket.MSG_BYTES_SIZE;
                ByteBuffer msgBuffer = ByteBuffer.allocate(msgSize);
                msgBuffer.put(bb);

                while (msgs_left > 0) {
                    packet_buffer = new byte[MulticastPacket.PACKET_SIZE];
                    packet = new DatagramPacket(packet_buffer, packet_buffer.length);
                    socket.receive(packet);

                    //Get packet bytes
                    bb = ByteBuffer.wrap(packet.getData());

                    //"Unpack" the bytes
                    downloader_id = bb.getInt();
                    seq_number = bb.getInt();
                    msgs_left = bb.getInt();
                    first_msg = bb.getInt();

                    msgBuffer.put(bb);

                }
                byte[] msgBytes = msgBuffer.array();
                int newLength; // Length without trailing zeros
                for(newLength = msgBytes.length - 1; newLength >= 0 && msgBytes[newLength] == 0; newLength--);

                String message = new String(msgBytes, 0, newLength + 1);
                System.out.println("Received from Downloader " + downloader_id + " " + message);
            }
        } catch (Exception e) {
            System.out.println("BarrelMulticastWorker " + id + " exception: " + e.getMessage());
        }
    }
}
