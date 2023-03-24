package IndexStorageBarrels;


import java.io.Serializable;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class BarrelsQueue implements Serializable {
    public static Queue<BarrelModule_S_I> barrelsqueue;

    public BarrelsQueue() {
        barrelsqueue = new ConcurrentLinkedQueue<>();
    }

    public synchronized Queue<BarrelModule_S_I> getBarrelsqueue() {
        return barrelsqueue;
    }

    public synchronized int getBarrelsqueueSize() {
        return barrelsqueue.size();
    }

    public synchronized boolean isEmptyBarrelsqueue() {
        int size = barrelsqueue.size();
        return size == 0;
    }

    public synchronized Iterator<BarrelModule_S_I> getBarrelsQueueIterator(){
        return barrelsqueue.iterator();
    }

    public synchronized void addToBarrelsqueue(BarrelModule_S_I barrel) {
        barrelsqueue.add(barrel);
    }

    public synchronized void removeFromBarrelsqueue(BarrelModule_S_I barrel) {
        barrelsqueue.remove(barrel);
    }
}
