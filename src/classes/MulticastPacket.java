package classes;

import java.nio.ByteBuffer;

public class MulticastPacket{
    public static final int PACKET_SIZE = 1000,
            HEADER_SIZE = 3 * 4,
            MSG_BYTES_SIZE = PACKET_SIZE - HEADER_SIZE;
    public int downloader_id, seq_number, msgs_left;
    public byte[] msgBytes;

    public MulticastPacket(int downloader_id, int seq_number, int msgs_left, byte[] msgBytes) {
        this.downloader_id = downloader_id;
        this.seq_number = seq_number;
        this.msgs_left = msgs_left;
        this.msgBytes = msgBytes.clone();
    }

    public byte[] toBytes(){
        try{
            ByteBuffer bb = ByteBuffer.allocate(PACKET_SIZE);
            bb.putInt(downloader_id);
            bb.putInt(seq_number);
            bb.putInt(msgs_left);
            bb.put(msgBytes);
            return bb.array();
        }
        catch(Exception e){
            System.out.println("MulticastPacket getBytes exception: " + e.getMessage());
        }
        return null;
    }
}
