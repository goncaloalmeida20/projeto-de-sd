package IndexStorageBarrels;

import java.net.MulticastSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.io.IOException;
import java.util.*;

import classes.Page;

public class Barrel {
    private String MULTICAST_ADDRESS = "224.3.2.1";
    private int PORT = 4321;
    private int id;
    private Thread t;
    private SearchServer ss;

    HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<String, ArrayList<Integer>>();
    HashMap<Integer, Page> all_pages = new HashMap<Integer, Page>();


    public Barrel(int i) {
        this.id = i;
        ss = new SearchServer();
        t = new Thread(ss);
        t.start();
    }
}