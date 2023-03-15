package IndexStorageBarrels;

import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import classes.Page;

public class SearchServer implements Runnable {
    public static int PORT0 = 1099;
    public static String arg0 = "search";
    private Thread t;

    public SearchServer() {
        t = new Thread(this);
        t.start();
    }

    public void run() {
        try {
            HashMap<String, ArrayList<Page>> invertedIndex = new HashMap<String, ArrayList<Page>>();

            SearchIf searchImpl = new SearchImpl(invertedIndex);
            Registry registry = LocateRegistry.createRegistry(PORT0);
            registry.rebind("search", searchImpl);

            System.out.println("Search server started...");


        } catch (Exception e) {
            System.err.println("Search server exception:" + e.getMessage());
        }
    }

    public static void main(String args[]) {
        try {
            HashMap<String, ArrayList<Page>> invertedIndex = new HashMap<String, ArrayList<Page>>();
            SearchImpl h = new SearchImpl(invertedIndex);
            LocateRegistry.createRegistry(PORT0).rebind(arg0, h);
            System.out.println("Search Server ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in SearchImpl.main: " + re);
        }
    }
}