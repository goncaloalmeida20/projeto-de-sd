package IndexStorageBarrels;

public class BarrelMulticast implements Runnable{
    public int id;
    public Thread t;

    public BarrelMulticast(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }
    public void run(){

    }
}
