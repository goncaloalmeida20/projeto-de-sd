package IndexStorageBarrels;

import java.rmi.RemoteException;
import java.util.*;

import classes.Page;

public class Barrel {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private int id;
    private Thread t;
    private BarrelModule ss;


    final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<String, ArrayList<Integer>>();
    final HashMap<Integer, Page> all_pages = new HashMap<Integer, Page>();


    public Barrel() throws RemoteException {
        ss = new BarrelModule(this);
        t = new Thread(ss);
        t.start();
    }

    public static void main(String[] args) throws RemoteException {
        Barrel barrel = new Barrel();
    }
}