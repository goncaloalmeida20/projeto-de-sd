package IndexStorageBarrels;

import classes.MulticastPacket;

import java.net.*;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

public class BarrelMulticastWorker implements Runnable{
    private final Map<Integer, ByteBuffer> downloadersBuffer= new HashMap<>();

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
                byte[] packetBuffer;
                synchronized (Barrel.bPageQueue){
                    while(Barrel.bPageQueue.size() == 0)
                        Barrel.bPageQueue.wait();
                    packetBuffer = Barrel.bPageQueue.remove(0);
                }
                //Get packet bytes
                ByteBuffer bb = ByteBuffer.wrap(packetBuffer);

                //"Unpack" the bytes
                int downloader_id = bb.getInt(), seq_number = bb.getInt(), msgs_left = bb.getInt(),
                        first_msg = bb.getInt();

                //If it's the first msg, allocate a buffer
                if(msgs_left == first_msg){
                    downloadersBuffer.put(downloader_id,
                            ByteBuffer.allocate((msgs_left + 1) * MulticastPacket.MSG_BYTES_SIZE));
                }

                downloadersBuffer.get(downloader_id).put(bb);

                if(msgs_left == 0){
                    byte[] msgBytes = downloadersBuffer.get(downloader_id).array();
                    int newLength; // Length without trailing zeros
                    for(newLength = msgBytes.length - 1; newLength >= 0 && msgBytes[newLength] == 0; newLength--);

                    String message = new String(msgBytes, 0, newLength + 1);
                    System.out.println("Received from Downloader " + downloader_id + " " + message);
                }
            }
        } catch (Exception e) {
            System.out.println("BarrelMulticastWorker " + id + " exception: " + e.getMessage());
        }
    }
}
