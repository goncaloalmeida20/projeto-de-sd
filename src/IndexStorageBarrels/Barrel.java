package IndexStorageBarrels;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.sql.SQLException;
import java.util.*;

import classes.Page;

public class Barrel{
    public static final String MULTICAST_ADDRESS = "224.0.1.0";
    public static final int MULTICAST_PORT = 5000;
    Thread t;
    private static BarrelModule ss;
    private static BarrelMulticastWorker bmw;

    private static BarrelMulticastRecovery bmr;
    private static BarrelMulticastReceiver bmrcv;
    public int id;

    //TODO: apagar isto para fazer os ficheiros de objetos
    public static final HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
    public static final HashMap<Integer, Page> all_pages = new HashMap<>();


    public Barrel(int id) throws RemoteException, NotBoundException, SQLException {
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

        ss = new BarrelModule();
        t = new Thread(ss);
        t.start();

        // Call the main method of DatabaseSetup to create the database
        String url = "jdbc:postgresql://localhost:5432/";
        String dbName = "Barrel" + id + "DB";
        String user = "postgres";
        String password = "postgres";
        DatabaseStarter.main(new String[]{url, dbName, user, password});
        System.out.println("Database created successfully");
    }

    public static void main(String[] args) throws NotBoundException, RemoteException, SQLException {
        int id = Integer.parseInt(args[0]);
        bmrcv = new BarrelMulticastReceiver(id);
        bmr = new BarrelMulticastRecovery(id);
        bmw = new BarrelMulticastWorker(id);
        //Barrel b = new Barrel(id);
        System.out.println("Barrel is ready");
    }
}