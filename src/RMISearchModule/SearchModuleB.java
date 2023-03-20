package RMISearchModule;

import IndexStorageBarrels.BarrelModule;
import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SearchModuleB implements Runnable, SearchModuleB_S_I{
    // RMI Barrel Info
    public static int PORT1 = 6942;
    public static String hostname1 = "127.0.0.2";

    private final SearchModule father;
    private BarrelModule barrelM;

    public Map<String, Integer> barrels;

    public SearchModuleB(SearchModule f) {
        super();
        barrels = Collections.synchronizedMap(new HashMap<>());
        father = f;
    }

    public void connect(BarrelModule bm) throws RemoteException {
        barrelM = bm;
    }

    public void run() {
        try {
            Registry rB = LocateRegistry.createRegistry(PORT1);
            rB.rebind(hostname1, this);

            System.out.println("Search Module - Barrel connection ready.");

            Map.Entry<Integer, HashMap<Object, Integer>> entry;
            HashMap<Object, Integer> task;

            while (true){
                entry = father.nextTask();
                task = new HashMap<>(entry.getValue());

                if (entry.getKey() == 1){
                    String[] terms = (String[]) task.keySet().toArray()[0];
                    int n_page = task.get(terms);
                    barrelM.search(terms, n_page);
                } else {
                    String url = (String) task.keySet().toArray()[0];
                    int n_page = task.get(url);
                    barrelM.search_pages(url, n_page);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
