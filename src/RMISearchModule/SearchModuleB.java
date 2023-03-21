package RMISearchModule;

import IndexStorageBarrels.BarrelModule;
import IndexStorageBarrels.BarrelModule_S_I;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.*;

public class SearchModuleB implements Runnable, SearchModuleB_S_I, Serializable {
    // RMI Barrel Info
    public static int PORT1 = 100;
    public static String hostname1 = "127.0.0.1";

    public List<BarrelModule_S_I> barrels;
    private SearchModule father;

    public SearchModuleB(SearchModule_S_I f) {
        super();
        barrels = Collections.synchronizedList(new ArrayList<>());
        System.out.println(f + " " + f.getClass());
        father = (SearchModule) f;
    }

    public void connect(BarrelModule_S_I bm) throws RemoteException {
        barrels.add(bm);
    }

    private Map.Entry<Integer, HashMap<Object, Integer>> nextTask() throws RemoteException{
        synchronized(father.tasks){
            try{
                while(father.tasks.size() == 0)
                    father.tasks.wait();
                return father.tasks.lastEntry();
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
            BarrelModule_S_I barrelM;
            Random rand = new Random();

            while (true){
                entry = nextTask();
                assert entry != null;
                task = new HashMap<>(entry.getValue());
                System.out.println(1);
                barrelM = barrels.get(rand.nextInt(barrels.size()));
                System.out.println(2);

                if (entry.getKey() == 1){
                    String[] terms = (String[]) task.keySet().toArray()[0];
                    int n_page = task.get(terms);
                    father.updateResultPages(barrelM.search(terms, n_page));
                } else {
                    String url = (String) task.keySet().toArray()[0];
                    int n_page = task.get(url);
                    father.updateResultPages(barrelM.search_pages(url, n_page));
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }
}
