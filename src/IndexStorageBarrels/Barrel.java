package IndexStorageBarrels;

import java.io.Serializable;
import java.util.*;

import classes.Page;

public class Barrel{
    private static BarrelModule ss;
    private static BarrelMulticast bMult;
    public int id;

    public static final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
    public static final HashMap<Integer, Page> all_pages = new HashMap<>();


    public Barrel(int id) {
        this.id = id;

    }

    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        bMult = new BarrelMulticast(id);

        ss = new BarrelModule(id);
        System.out.println("Barrel is ready");
    }
}