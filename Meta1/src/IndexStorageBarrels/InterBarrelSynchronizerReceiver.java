package IndexStorageBarrels;

import classes.MulticastPacket;

import java.net.*;
import java.nio.ByteBuffer;

public class InterBarrelSynchronizerReceiver implements Runnable {
    public int id;
    public Thread t;
    public InterBarrelSynchronizerReceiver(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("InterBarrelSynchronizerReceiver " + id);

        try (MulticastSocket socket = new MulticastSocket(Barrel.SYNC_MULTICAST_PORT)) {
            // create socket and bind it
            InetAddress mcastaddr = InetAddress.getByName(Barrel.SYNC_MULTICAST_ADDRESS);
            InetSocketAddress group = new InetSocketAddress(mcastaddr, Barrel.SYNC_MULTICAST_PORT);
            NetworkInterface netIf = NetworkInterface.getByName("bge0");

            // join group
            socket.joinGroup(group, netIf);

            boolean first = true;

            while (true) {
                //receive multicast packet
                byte[] packet_buffer = new byte[MulticastPacket.PACKET_SIZE];
                DatagramPacket packet = new DatagramPacket(packet_buffer, packet_buffer.length);
                if(first){
                    synchronized (InterBarrelSynchronizerInserter.syncLock){
                        InterBarrelSynchronizerInserter.needSync = 1;
                        InterBarrelSynchronizerInserter.syncLock.notify();
                    }
                    first = false;
                }
                socket.receive(packet);

                byte[] packetBytes = packet.getData();

                //Get packet header bytes
                ByteBuffer headerBytes = ByteBuffer.wrap(packetBytes, 0, MulticastPacket.HEADER_SIZE);

                //get the header bytes
                int barrelId = headerBytes.getInt(), senderId = headerBytes.getInt(), msgType = headerBytes.getInt(),
                        msgsLeft = headerBytes.getInt(), ack = headerBytes.getInt();

                System.out.println("SYNC Received " + barrelId + " " + senderId + " " + msgType + " " + msgsLeft
                + " " + ack);

                //check if the message target isn't this buffer
                if(barrelId > 0 && barrelId != id || senderId == id) continue;

                //redirect the packet to the helper or inserter depending on the header
                if(barrelId == -1 || msgType == -1 || msgType == -3 || msgType == -4 || ack == -1){
                    //Send to helper
                    synchronized (InterBarrelSynchronizerHelper.helperQueue){
                        InterBarrelSynchronizerHelper.helperQueue.add(packetBytes);
                        InterBarrelSynchronizerHelper.helperQueue.notify();
                    }
                }
                else{
                    //Send to inserter
                    synchronized (InterBarrelSynchronizerInserter.inserterQueue){
                        InterBarrelSynchronizerInserter.inserterQueue.add(packetBytes);
                        InterBarrelSynchronizerInserter.inserterQueue.notify();
                    }
                }

            }
        }
        catch(Exception e){
            System.out.println("InterBarrelSynchronizerReceiver" + id + " exception: " + e + " - " + e.getMessage());
        }
    }
}
