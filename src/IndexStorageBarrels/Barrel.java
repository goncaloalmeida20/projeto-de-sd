package IndexStorageBarrels;

import java.io.Serializable;
import java.util.*;

import classes.Page;

public class Barrel implements Serializable {
    private Thread t;
    private BarrelModule ss;
    private BarrelMulticast bMult;
    public int id;

    final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
    final HashMap<Integer, Page> all_pages = new HashMap<>();


    public Barrel(int id) {
        this.id = id;
        bMult = new BarrelMulticast(id);

        ss = new BarrelModule(this);
    }

    public static void main(String[] args) {
        Barrel barrel = new Barrel(Integer.parseInt(args[0]));
        System.out.println("Barrel is ready");
    }
}