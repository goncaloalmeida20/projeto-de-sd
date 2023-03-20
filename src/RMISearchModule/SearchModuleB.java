package RMISearchModule;

import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SearchModuleB implements Runnable, SearchModuleB_S_I{
    // RMI Barrel Info
    public static int PORT1 = 6942;
    public static String hostname1 = "127.0.0.2";

    public Map<String, Integer> barrels;

    public SearchModuleB() {
        super();
        barrels = Collections.synchronizedMap(new HashMap<>());
    }

    public void run() {
        try {
            SearchModuleB searchBarrel = new SearchModuleB();
            Registry rB = LocateRegistry.createRegistry(PORT1);
            rB.rebind(hostname1, searchBarrel);

            System.out.println("Search Module - Barrel connection ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
