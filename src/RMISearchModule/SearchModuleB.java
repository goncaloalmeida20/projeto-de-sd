package RMISearchModule;

import IndexStorageBarrels.BarrelModule;
import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.RemoteServer;
import java.rmi.server.ServerNotActiveException;
import java.util.*;

public class SearchModuleB implements Runnable, SearchModuleB_S_I{
    // RMI Barrel Info
    public static int PORT1 = 100;
    public static String hostname1 = "127.0.0.1";

    private final NavigableMap<Integer, HashMap<Object, Integer>> tasks;
    public Map<BarrelModule, String> barrels;

    public SearchModuleB(NavigableMap<Integer, HashMap<Object, Integer>> t) {
        super();
        barrels = Collections.synchronizedMap(new HashMap<>());
        tasks = t;
    }

    public void connect(BarrelModule bm) throws RemoteException, ServerNotActiveException {
        barrels.put(bm, RemoteServer.getClientHost());
    }

    private Map.Entry<Integer, HashMap<Object, Integer>> nextTask() throws RemoteException{
        synchronized(tasks){
            try{
                while(tasks.size() == 0)
                    tasks.wait();
                return tasks.lastEntry();
            }
            catch(Exception e){
                System.out.println("Tasks exception: " + e.getMessage());
            }
        }
        return null;
    }

    public void run() {
        try {
            Registry rB = LocateRegistry.createRegistry(PORT1);
            rB.rebind(hostname1, this);

            System.out.println("Search Module - Barrel connection ready.");

            Map.Entry<Integer, HashMap<Object, Integer>> entry;
            HashMap<Object, Integer> task;

            // TODO: SELECT BARREL TO PROCEDE
            while (true){
                entry = nextTask();
                assert entry != null;
                task = new HashMap<>(entry.getValue());

                if (entry.getKey() == 1){
                    String[] terms = (String[]) task.keySet().toArray()[0];
                    int n_page = task.get(terms);
                    //barrelM.search(terms, n_page);
                } else {
                    String url = (String) task.keySet().toArray()[0];
                    int n_page = task.get(url);
                    //barrelM.search_pages(url, n_page);
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
