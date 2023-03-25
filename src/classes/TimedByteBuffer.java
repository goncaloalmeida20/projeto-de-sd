package classes;

import java.nio.ByteBuffer;

public class TimedByteBuffer {
    public static final long TIMEOUT_MS = 2000;
    public ByteBuffer byteBuffer;
    public long creationTime;

    public TimedByteBuffer(ByteBuffer byteBuffer, long creationTime) {
        this.byteBuffer = byteBuffer;
        this.creationTime = creationTime;
    }

    public long timeSinceCreation(){
        return System.currentTimeMillis() - creationTime;
    }
}
