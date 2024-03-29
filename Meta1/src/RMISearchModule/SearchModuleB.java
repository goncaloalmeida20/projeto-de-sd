package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.net.SocketException;
import java.rmi.ConnectException;
import java.rmi.RemoteException;
import java.rmi.UnmarshalException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.*;

/**
 * Represents a Search Module Barrel, which is used to connect to other Barrel Modules
 * and execute search tasks in parallel.
 */
public class SearchModuleB extends UnicastRemoteObject implements SearchModuleB_S_I, Runnable {
    public SearchModuleB searchModuleB;

    // RMI Barrel info
    public static int PORT = 7002;
    public static String hostname = "this";

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public final HashMap<SearchModuleC, List<HashMap<Integer, String>>> resultsTopTen;

    /**
     * Creates a new instance of the Search Module Barrel.
     *
     * @param t   A map of search tasks, where each task is associated with a set of search parameters and a client ID.
     * @param p   A map of search results, where each result is associated with a client ID.
     * @param rtt A map of search results of the top 10 searches, where each result is associated with a client ID.
     * @throws RemoteException If there is an error with the remote connection.
     */
    public SearchModuleB(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p, HashMap<SearchModuleC, List<HashMap<Integer, String>>> rtt) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;
        this.resultsTopTen = rtt;
    }

    /**
     * Connects a Barrel to the Search Module and returns the id of the barrel.
     *
     * @param barrel The Barrel to connect to the Search Module.
     * @param id     The ID of the barrel.
     * @return Integer representing the ID assigned to the connected barrel.
     * @throws RemoteException If there is an error with the remote connection.
     */
    public int connect(BarrelModule_S_I barrel, int id) throws RemoteException {
        ++SearchModule.sI.bAllCounter;
        //System.out.println("Connecting Barrel " + SearchModule.sI.bAllCounter);
        System.out.println("Connecting Barrel " + id);
        synchronized (SearchModule.sI.barrels) {
            SearchModule.sI.barrels.add(barrel);
            System.out.println("Number of barrels connected: " + SearchModule.sI.barrels.size());
        }
        return SearchModule.sI.bAllCounter;
    }

    /**
     * Disconnects a Barrel from the SearchModule by removing it from the barrels list.
     * Note that this function is called if a Barrel breaks down.
     *
     * @param index The index of the Barrel to be disconnected (removed from the barrels list).
     */
    private void disconnect(int index) {
        synchronized (SearchModule.sI.barrels) {
            System.out.println("A barrel disconnected");
            SearchModule.sI.barrels.remove(index);
        }
    }

    /**
     * Returns a random Barrel from the Search Module's list of connected barrels.
     *
     * @param index The index of the Barrel to be returned.
     * @return A random Barrel or null if there are no active clients.
     */
    private BarrelModule_S_I getRandomBarrelModule(int index) {
        synchronized (SearchModule.sI.barrels) {
            if (SearchModule.sI.barrels.isEmpty()) {
                return null; // No active clients
            }
            //System.out.println("Index: " + index + " Size: " + SearchModule.sI.barrels.size());
            return SearchModule.sI.barrels.get(index);
        }
    }

    private int getBarrelsListSize() {
        synchronized (SearchModule.sI.barrels) {
            if (SearchModule.sI.barrels.isEmpty()) {
                return 0;
            }
            return SearchModule.sI.barrels.size();
        }
    }

    // =======================================================

    /**
     * Starts the Search Module Barrel and executes search tasks in parallel.
     */
    @Override
    public void run() {
        BarrelModule_S_I barrelM = null;
        int randomIndex = 0;
        try {
            searchModuleB = new SearchModuleB(tasks, result_pages, resultsTopTen);
            Registry r = LocateRegistry.createRegistry(PORT);
            r.rebind(hostname, searchModuleB);
            System.out.println("Search Module - Barrel connection ready.");
        } catch (RemoteException e) {
            throw new RuntimeException(e);
        }

        while (true) {
            try {
                HashMap<Object, Integer> task;

                /*
                 * The code runs in a loop, and within the loop, there is another loop that executes tasks.
                 * The tasks are stored in a HashMap object, and it synchronizes access to this object
                using the synchronized keyword. It is used wait and notifyAll methods to coordinate the
                execution of the tasks between multiple threads.
                 * Within the task execution loop, it is randomly selected a barrel module from a list,
                and then executes the task using the randomly selected barrel.
                 * After the task is executed, the result is stored in another HashMap object
                called "result_pages" or "resultTopTen", which is also synchronized.
                 * Finally, the task is removedfrom the "tasks" HashMap.
                **/
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
                            long startTime = System.currentTimeMillis();
                            long elapsedTime = 0;
                            boolean barrelFound = false;
                            while (elapsedTime < 5000 && !barrelFound) {
                                synchronized (SearchModule.sI.barrels) {
                                    if (!SearchModule.sI.barrels.isEmpty()) {
                                        randomIndex = (int) (Math.random() * SearchModule.sI.barrels.size());
                                        barrelM = getRandomBarrelModule(randomIndex);
                                        if (barrelM != null) {
                                            barrelFound = true;
                                        }
                                    }
                                }
                                elapsedTime = System.currentTimeMillis() - startTime;
                            }
                            int type = key.values().iterator().next();
                            synchronized (tasks) {
                                task = tasks.get(key);
                            }
                            if (barrelFound) {
                                // System.out.println("Type error");
                                // System.out.println("Type: " + type);
                                // System.out.println("Task: " + task + " Type: " + type);
                                if (type == 1) {
                                    try {
                                        ArrayList<Page> res = barrelM.search((String[]) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                                        synchronized (result_pages) {
                                            for (SearchModuleC client : key.keySet()) {
                                                result_pages.put(client, res);
                                                result_pages.notifyAll();
                                            }
                                        }
                                    } catch (RemoteException e) {
                                        synchronized (result_pages) {
                                            for (SearchModuleC client : key.keySet()) {
                                                result_pages.put(client, null);
                                                result_pages.notifyAll();
                                            }
                                        }
                                        throw new SocketException();
                                    }
                                } else if (type == 2) {
                                    try {
                                        ArrayList<Page> res = barrelM.search_pages((String) task.keySet().toArray()[0], (int) task.values().toArray()[0]);
                                        synchronized (result_pages) {
                                            for (SearchModuleC client : key.keySet()) {
                                                result_pages.put(client, res);
                                                result_pages.notifyAll();
                                            }
                                        }
                                    } catch (RemoteException e) {
                                        synchronized (result_pages) {
                                            for (SearchModuleC client : key.keySet()) {
                                                result_pages.put(client, null);
                                                result_pages.notifyAll();
                                            }
                                        }
                                        throw new SocketException();
                                    }
                                } else if (type == 3) {
                                    try {
                                        List<HashMap<Integer, String>> res = barrelM.getTopTenSearches();
                                        synchronized (resultsTopTen) {
                                            for (SearchModuleC client : key.keySet()) {
                                                resultsTopTen.put(client, res);
                                                resultsTopTen.notifyAll();
                                            }
                                        }
                                    } catch (RemoteException e) {
                                        synchronized (resultsTopTen) {
                                            for (SearchModuleC client : key.keySet()) {
                                                resultsTopTen.put(client, null);
                                                resultsTopTen.notifyAll();
                                            }
                                        }
                                        throw new SocketException();
                                    }
                                }
                                tasks.remove(key);
                            } else {
                                if (type == 1 || type == 2) {
                                    synchronized (result_pages) {
                                        for (SearchModuleC client : key.keySet()) {
                                            result_pages.put(client, null);
                                            result_pages.notifyAll();
                                        }
                                    }
                                } else if (type == 3) {
                                    synchronized (resultsTopTen) {
                                        for (SearchModuleC client : key.keySet()) {
                                            resultsTopTen.put(client, null);
                                            resultsTopTen.notifyAll();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (SocketException ex) {
                disconnect(randomIndex);
                System.out.println("The barrel that was responding to the client has been disconnected in the middle of the search.");
                //System.out.println("Trying with another Barrel");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
