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

    private Map.Entry<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> nextTask() throws RemoteException{
        synchronized(tasks){
            try{
                while(tasks.size() == 0)
                    tasks.wait();
                List<Map.Entry<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>>> entryList = new ArrayList<>(tasks.entrySet());
                return entryList.get(entryList.size()-1);
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

            Map.Entry<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> entry;
            HashMap<Object, Integer> task;
            HashMap<SearchModuleC, Integer> cliendThreadAndTaskType;
            ArrayList<Page> p;
            BarrelModule_S_I barrelM;
            int n_page, taskType;
            SearchModuleC clientThread;
            Random rand = new Random();

            while (true){
                entry = nextTask();
                assert entry != null;
                task = new HashMap<>(entry.getValue());
                cliendThreadAndTaskType = new HashMap<>(entry.getKey());
                barrelM = barrels.get(rand.nextInt(barrels.size()));
                clientThread = (SearchModuleC) cliendThreadAndTaskType.keySet().toArray()[0];
                taskType = cliendThreadAndTaskType.get(cliendThreadAndTaskType.keySet().toArray()[0]);

                if (taskType == 1){
                    String[] terms = (String[]) task.keySet().toArray()[0];
                    n_page = task.get(terms);
                    p = barrelM.search(terms, n_page);
                    synchronized (clientThread){
                        synchronized (result_pages){
                            result_pages.put(clientThread, p);
                        }
                        clientThread.notify();
                    }

                } else {
                    String url = (String) task.keySet().toArray()[0];
                    n_page = task.get(url);
                    p = barrelM.search_pages(url, n_page);
                    synchronized (clientThread){
                        synchronized (result_pages){
                            result_pages.put(clientThread, p);
                        }
                        clientThread.notify();
                    }
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
