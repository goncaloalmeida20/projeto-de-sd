package IndexStorageBarrels;

public class BarrelMulticast implements Runnable{
    public Thread t;
    public int id;
    public BarrelMulticast(int id){
        this.id = id;
        t = new Thread(this);
        t.start();
    }

    public void run(){
        System.out.println("BarrelMulticast " + id);
    }
}
