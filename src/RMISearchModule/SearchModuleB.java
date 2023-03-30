package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.io.*;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.*;

public class SearchModuleB extends UnicastRemoteObject implements SearchModuleB_S_I, Runnable {
    public SearchModuleB searchModuleB;
    // RMI Barrel info
    public static int PORT = 7002;
    public static String hostname = "this";

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;


    public SearchModuleB(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;

    }

    /**
     * Connects a Barrel to the Search Module and returns the id of the barrel
     * @param barrel Barrel to connect to the Search Module
     * @return Integer representing the id assigned to the connected barrel
     * @throws RemoteException If there is an error with the remote connection
     */
    public int connect(BarrelModule_S_I barrel) throws RemoteException {
        ++SearchModule.sI.bAllCounter;
        System.out.println("Connecting Barrel " + SearchModule.sI.bAllCounter);
        synchronized (SearchModule.sI.barrels) {
            SearchModule.sI.barrels.add(barrel);
            System.out.println("Number of barrels: " + SearchModule.sI.barrels.size());
        }
        try {
            FileOutputStream fileOut = new FileOutputStream("src/databases/barrelId.ser");
            ObjectOutputStream Objout = new ObjectOutputStream(fileOut);
            Objout.writeObject(SearchModule.sI.bAllCounter);

            Objout.close();
            fileOut.close();

            System.out.println("BarrelId object saved in barrelId.ser");
        } catch (IOException e) {
            System.out.println("Barrel id save: " + e.getMessage());
        }
        return SearchModule.sI.bAllCounter;
    }
    
    /**
     * Disconnects a Barrel from the SearchModule by removing it from the barrels list
     * Note that this function is called if a Barrel breakdown
     * @param index Index of the Barrel to be disconnected (Removed from the barrels list)
     */
    private void disconnect(int index) {
        synchronized (SearchModule.sI.barrels){
            System.out.println("A barrel disconnected");
            SearchModule.sI.barrels.remove(index);
        }
    }

    /**
     * Returns a random Barrel from the Search Module's list of connected barrels
     * @param index Index of the Barrel to be returned
     * @return a random Barrel or null if there are no active clients
     */
    private BarrelModule_S_I getRandomBarrelModule(int index) {
        synchronized (SearchModule.sI.barrels) {
            if (SearchModule.sI.barrels.isEmpty()) {
                return null; // No active clients
            }
            System.out.println("Index: " + index + " Size: " + SearchModule.sI.barrels.size());
            return SearchModule.sI.barrels.get(index);
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
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        for (HashMap<SearchModuleC, Integer> key : tasks.keySet()) {
                            synchronized (SearchModule.sI.barrels){
                                randomIndex = (int) (Math.random() * SearchModule.sI.barrels.size());
                            }
                            barrelM = getRandomBarrelModule(randomIndex);
                            if (barrelM != null) {
                                // System.out.println("Type error");
                                int type = key.values().iterator().next();
                                // System.out.println("Type: " + type);
                                synchronized (tasks){
                                    task = tasks.get(key);
                                }
                                // System.out.println("Task: " + task + " Type: " + type);
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
                disconnect(randomIndex);
            } catch (RemoteException | SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
