package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SearchModuleB extends UnicastRemoteObject implements SearchModuleB_S_I, Runnable {
    private int bAllCounter = 0;

    public SearchModuleB searchModuleB;
    // RMI Barrel info
    public static int PORT = 7002;
    public static String hostname = "this";

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
     * Connects a Barrel to the Search Module and returns the id of the barrel
     * @param b Barrel to connect to the Search Module
     * @return Integer representing the id assigned to the connected barrel
     * @throws RemoteException If there is an error with the remote connection
     */
    public int connect(BarrelModule_S_I b) throws RemoteException {
        bAllCounter++;
        System.out.println("Connecting Barrel " + bAllCounter);
        synchronized (barrels) {
            barrels.add(b);
            System.out.println("Number of barrels: " + barrels.size());
        }
        return bAllCounter;
    }
    
    /**
     * Disconnects a Barrel from the SearchModule by removing it from the barrels list
     * Note that this function is called if a Barrel breakdown
     * @param index Index of the Barrel to be disconnected (Removed from the barrels list)
     * @throws RemoteException If there is an error with the remote connection
     */
    private void disconnect(int index) throws RemoteException {
        synchronized (searchModuleB.barrels){
            System.out.println("A barrel disconnected");
            searchModuleB.barrels.remove(index);
        }
    }

    /**
     * Returns a random Barrel from the Search Module's list of connected barrels
     * @param index Index of the Barrel to be returned
     * @return a random Barrel or null if there are no active clients
     */
    private BarrelModule_S_I getRandomBarrelModule(int index) {
        synchronized (searchModuleB.barrels) {
            if (searchModuleB.barrels.isEmpty()) {
                return null; // No active clients
            }
            Iterator<BarrelModule_S_I> iter = searchModuleB.barrels.iterator();
            for (int i = 0; i < index; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

    // =======================================================
    @Override
    public void run() {
        BarrelModule_S_I barrelM;
        int randomIndex = 0;
        try {
            searchModuleB = new SearchModuleB(tasks, result_pages);
            Registry r = LocateRegistry.createRegistry(PORT);
            r.rebind(hostname, searchModuleB);
            System.out.println("Search Module - Barrel connection ready.");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                HashMap<Object, Integer> task;

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
                            synchronized (searchModuleB.barrels){
                                randomIndex = (int) (Math.random() * searchModuleB.barrels.size());
                            }
                            barrelM = getRandomBarrelModule(randomIndex);
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
            } catch (ConnectException e ) {
                try {
                    disconnect(randomIndex);
                } catch (RemoteException ex) {
                    throw new RuntimeException(ex);
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }
}
