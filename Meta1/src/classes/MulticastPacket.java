package classes;

import java.nio.ByteBuffer;

public class MulticastPacket{
    public static final int PACKET_SIZE = 1000,
            HEADER_SIZE = 5 * 4, //5 integers
            MSG_BYTES_SIZE = PACKET_SIZE - HEADER_SIZE;

    //Note: This order of the header is the default multicast packet order used in the downloader and barrel
    //communication. It may differ depending on the circumstances (for example, in the barrel synchronization)
    public int barrelId, downloaderId, seqNumber, msgsLeft, firstMsg;
    public byte[] msgBytes;

    public MulticastPacket(int barrelId, int downloaderId, int seqNumber, int msgsLeft, int firstMsg) {
        this.barrelId = barrelId;
        this.downloaderId = downloaderId;
        this.seqNumber = seqNumber;
        this.msgsLeft = msgsLeft;
        this.firstMsg = firstMsg;
        msgBytes = null;
    }

    public MulticastPacket(int barrelId, int downloaderId, int seqNumber, int msgsLeft, int firstMsg, byte[] msgBytes) {
        this.barrelId = barrelId;
        this.downloaderId = downloaderId;
        this.seqNumber = seqNumber;
        this.msgsLeft = msgsLeft;
        this.firstMsg = firstMsg;
        this.msgBytes = msgBytes.clone();
    }


    /**
     * Turns this packet into a byte array
     * @return this packet in bytes
     */
    public byte[] toBytes(){
        try{
            ByteBuffer bb = ByteBuffer.allocate(PACKET_SIZE);
            bb.putInt(barrelId);
            bb.putInt(downloaderId);
            bb.putInt(seqNumber);
            bb.putInt(msgsLeft);
            bb.putInt(firstMsg);
            if(msgBytes != null)
                bb.put(msgBytes);
            return bb.array();
        }
        catch(Exception e){
            System.out.println("MulticastPacket getBytes exception: " + e.getMessage());
        }
        return null;
    }
}
