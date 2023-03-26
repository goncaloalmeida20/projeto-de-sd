package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SearchModuleB extends UnicastRemoteObject implements SearchModuleB_S_I, Runnable {
    private int bAllCounter = 0;

    public SearchModuleB h;

    public final ArrayList<BarrelModule_S_I> barrels;

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;


    public SearchModuleB(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;
        barrels = new ArrayList<>();
    }

    /**
     Connects a Barrel to the Search Module and returns the id of the barrel
     @param b Barrel to connect to the Search Module
     @return Integer representing the id assigned to the connected barrel
     @throws RemoteException If there is an error with the remote connection
     */
    public synchronized int connect(BarrelModule_S_I b) throws RemoteException {
        bAllCounter++;
        System.out.println("Connecting Barrel " + bAllCounter);
        synchronized (barrels) {
            barrels.add(b);
        }
        return bAllCounter;
    }

    /**
     Disconnects a Barrel from the SearchModule by removing it from the barrels list
     Note that this function can be called if a Barrel breakdown
     @param b b Barrel to disconnect from the Search Module
     @throws RemoteException If there is an error with the remote connection
     */
    public void disconnect(BarrelModule_S_I b) throws RemoteException {
        System.out.println("Barrel " + b.getId() + " disconnected");
        synchronized (barrels) {
            barrels.remove(b);
        }
    }

    /**
     Returns a random Barrel from the Search Module's list of connected barrels
     @return a random Barrel or null if there are no active clients
     */
    private BarrelModule_S_I getRandomBarrelModule() {
        synchronized (h.barrels) {
            if (h.barrels.isEmpty()) {
                return null; // No active clients
            }
            int randomIndex = (int) (Math.random() * h.barrels.size());
            Iterator<BarrelModule_S_I> iter = h.barrels.iterator();
            for (int i = 0; i < randomIndex; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

    // =======================================================
    @Override
    public void run() {
        try {
            h = new SearchModuleB(tasks, result_pages);
            Registry r = LocateRegistry.createRegistry(7002);
            r.rebind("XPT", h);

            System.out.println("Search Module - Barrel connection ready.");

            HashMap<Object, Integer> task;
            BarrelModule_S_I barrelM;

            while (true) {
                synchronized (tasks) {
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                            //System.out.println("Number of barrels: " + h.barrels.size());
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    for (HashMap<SearchModuleC, Integer> key : tasks.keySet()) {
                        barrelM = getRandomBarrelModule();
                        if (barrelM != null) {
                            int type = key.values().iterator().next();
                            task = tasks.get(key);
                            if (type == 1) {
                                ArrayList<Page> res = barrelM.search((String[]) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                                synchronized (result_pages) {
                                    for (SearchModuleC client : key.keySet()) {
                                        result_pages.put(client, res);
                                        result_pages.notifyAll();
                                    }
                                }
                            } else if (type == 2) {
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
