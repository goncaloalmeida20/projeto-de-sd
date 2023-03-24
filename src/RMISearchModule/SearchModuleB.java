package RMISearchModule;

import IndexStorageBarrels.BarrelModule;
import IndexStorageBarrels.BarrelModule_S_I;
import IndexStorageBarrels.BarrelsQueue;
import classes.Page;

import java.io.Serializable;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SearchModuleB implements Runnable, SearchModuleB_S_I, Serializable {
    // RMI Barrel Info
    public static int PORT1 = 100;
    public static String hostname1 = "127.0.0.1";

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    private final Queue<BarrelModule_S_I> barrels;

    public SearchModuleB(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) {
        super();
        tasks = t;
        result_pages = p;
        barrels = new ConcurrentLinkedQueue<>();
    }

    public void connect(BarrelModule_S_I bm) throws RemoteException{
        synchronized (barrels) {
            barrels.add(bm);
        }
        System.out.println(barrels.size());
    }

    private BarrelModule_S_I getRandomBarrelModule() {
        synchronized (barrels) {
            if (barrels.isEmpty()) {
                return null; // No active clients
            }
            int randomIndex = (int) (Math.random() * barrels.size());
            Iterator<BarrelModule_S_I> iter = barrels.iterator();
            for (int i = 0; i < randomIndex; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

    public void run() {
        try {
            Registry rB = LocateRegistry.createRegistry(PORT1);
            rB.rebind(hostname1, this);

            System.out.println("Search Module - Barrel connection ready.");

            HashMap<Object, Integer> task;
            BarrelModule_S_I barrelM;

            while (true) {
                synchronized (tasks) {
                    //System.out.println(7);
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                            System.out.println("Number of barrels: " + barrels.size());
                            //System.out.println(8);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    //System.out.println(9);
                    for (HashMap<SearchModuleC, Integer> key : tasks.keySet()) {
                        //System.out.println(13);
                        barrelM = getRandomBarrelModule();
                        if (barrelM != null) {
                            System.out.println(10);
                            int type = key.values().iterator().next();
                            System.out.println(11 + " " + type);
                            task = tasks.get(key);
                            if (type == 1) {
                                System.out.println(12);
                                ArrayList<Page> res = barrelM.search((String[]) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                                System.out.println(2);
                                synchronized (result_pages) {
                                    for (SearchModuleC client : key.keySet()) {
                                        result_pages.put(client, res);
                                        result_pages.notifyAll();
                                    }
                                }
                            } else if (type == 2) {
                                System.out.println(13);
                                ArrayList<Page> res = barrelM.search_pages((String) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                                synchronized (result_pages) {
                                    for (SearchModuleC client : key.keySet()) {
                                        result_pages.put(client, res);
                                        result_pages.notifyAll();
                                    }
                                }
                            }
                            tasks.remove(key);
                        }
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
