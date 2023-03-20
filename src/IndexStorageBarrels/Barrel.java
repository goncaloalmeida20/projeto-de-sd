package IndexStorageBarrels;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.rmi.RemoteException;
import java.util.*;

import classes.Page;

public class Barrel {
    private final String MULTICAST_ADDRESS = "224.3.2.1";
    private final int PORT = 4321;
    private int id;
    private Thread t;
    private SearchServer ss;

    public HashMap<String, Integer> downloaders;
    public HashMap<String, Integer> barrels;


    HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<String, ArrayList<Integer>>();
    HashMap<Integer, Page> all_pages = new HashMap<Integer, Page>();


    public Barrel(int i) throws RemoteException {
        this.id = i;
        ss = new SearchServer(invertedIndex, all_pages);
        t = new Thread(ss);
        t.start();
    }
}