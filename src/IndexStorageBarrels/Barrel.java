package IndexStorageBarrels;

import java.util.*;

import classes.Page;

public class Barrel {
    private Thread t;
    private BarrelModule ss;


    final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
    final HashMap<Integer, Page> all_pages = new HashMap<>();


    public Barrel() {
        ss = new BarrelModule(this);
        t = new Thread(ss);
        t.start();
    }

    public static void main(String[] args) {
        Barrel barrel = new Barrel();
        System.out.println("Barrel is ready");
    }
}