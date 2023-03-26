package classes;

import java.nio.ByteBuffer;

public class TimedByteBuffer {
    public static final long TIMEOUT_MS = 2000;
    public ByteBuffer byteBuffer;
    public long creationTime;

    public TimedByteBuffer(){
        this.byteBuffer = null;
        creationTime = System.currentTimeMillis();
    }

    public TimedByteBuffer(ByteBuffer byteBuffer) {
        this.byteBuffer = byteBuffer;
        creationTime = System.currentTimeMillis();
    }

    public TimedByteBuffer(ByteBuffer byteBuffer, long creationTime) {
        this.byteBuffer = byteBuffer;
        this.creationTime = creationTime;
    }



    public long timeSinceCreation(){
        return System.currentTimeMillis() - creationTime;
    }
}
