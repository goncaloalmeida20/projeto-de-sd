package IndexStorageBarrels;

import java.nio.ByteBuffer;
import java.util.*;

import classes.Page;

public class Barrel{
    private static BarrelModule ss;
    private static BarrelMulticastWorker bmw;

    private static BarrelMulticast bm;
    public int id;

    public static final Map<Integer, Byte[]> bPageQueue = Collections.synchronizedMap(new HashMap<>());;

    //TODO: apagar isto para fazer os ficheiros de objetos
    public static final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
    public static final HashMap<Integer, Page> all_pages = new HashMap<>();


    public Barrel(int id) {
        this.id = id;

        int id_page = 1;
        List<String> s = new ArrayList<>();
        s.add("this");
        s.add("is");
        s.add("google");
        List<String> l = new ArrayList<>();
        s.add("bing.pt");
        s.add("yahoo.pt");
        Page p = new Page("google.pt", "Google", "this is google", s , l);
        ArrayList<Integer> ids = new ArrayList<>();
        ids.add(id_page);
        invertedIndex.put("this", ids);
        invertedIndex.put("is", ids);
        invertedIndex.put("google", ids);
        all_pages.put(id_page, p);
    }

    public static void main(String[] args) {
        int id = Integer.parseInt(args[0]);
        bm = new BarrelMulticast(id);
        bmw = new BarrelMulticastWorker(id);
        //ss = new BarrelModule(id);
        System.out.println("Barrel is ready");
    }
}