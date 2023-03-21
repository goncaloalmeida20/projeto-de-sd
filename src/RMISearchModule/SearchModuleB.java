package RMISearchModule;

import IndexStorageBarrels.BarrelModule;
import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.io.Serializable;
import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SearchModuleB implements Runnable, SearchModuleB_S_I, Serializable {
    // RMI Barrel Info
    public static int PORT1 = 100;
    public static String hostname1 = "127.0.0.1";

    public List<BarrelModule_S_I> barrels;
    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    public SearchModuleB(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) {
        super();
        barrels = Collections.synchronizedList(new ArrayList<>());
        tasks = t;
        result_pages = p;
    }

    public void connect(BarrelModule_S_I bm) throws RemoteException {
        barrels.add(bm);
    }

    public void run() {
        try {
            Registry rB = LocateRegistry.createRegistry(PORT1);
            rB.rebind(hostname1, this);

            System.out.println("Search Module - Barrel connection ready.");

            HashMap<Object, Integer> task;
            BarrelModule_S_I barrelM;
            Random rand = new Random();

            while (true){
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                for (HashMap<SearchModuleC, Integer> key : tasks.keySet()) {
                    if (barrels.size() > 0) {
                        int randomIndex = rand.nextInt(barrels.size());
                        barrelM = barrels.get(randomIndex);
                    int type = key.values().iterator().next();
                    task = tasks.get(key);
                    if (type == 1) {
                        ArrayList<Page> res = barrelM.search((String[]) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                        synchronized(result_pages) {
                            for (SearchModuleC client : key.keySet()) {
                                result_pages.put(client, res);
                                result_pages.notifyAll();
                            }
                        }
                    } else if (type == 2) {
                        ArrayList<Page> res = barrelM.search_pages((String) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                        synchronized(result_pages) {
                            for (SearchModuleC client : key.keySet()) {
                                result_pages.put(client, res);
                                result_pages.notifyAll();
                            }
                        }
                    }
                    tasks.remove(key);
                }}
            }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
