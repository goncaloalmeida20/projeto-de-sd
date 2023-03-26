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

    public void print_on_server(String s , BarrelModule_S_I c) throws RemoteException {

        System.out.println("> " + s);

        /**
         * Inicia conexao entre search module e storage barrel
         */
        if(s.equals("search")){
            try{
                SearchModuleB_S_I server = (SearchModuleB_S_I) LocateRegistry.getRegistry(7002).lookup("XPTO");
                server.subscribe((BarrelModule_S_I) h);
                synchronized (barrels) {
                    Random rand = new Random();
                    int rand_int = rand.nextInt(barrels.size() - 1);

                    System.out.println(barrels.size() + " |||| " + rand_int);

                    BarrelModule_S_I client = barrels.get(rand_int);
                    client.print_on_client("search_start");

                    server.print_on_server("Enviei uma mensagem|", (BarrelModule_S_I) h);
                    server.unsubscribe("Barrels Server", (BarrelModule_S_I) h);
                }
            }catch(Exception re){
                System.out.println("Error");
            }
        }
    }

    public int subscribe(BarrelModule_S_I c) throws RemoteException {
        bAllCounter++;
        System.out.println("Coonecting Barrel " + bAllCounter);
        System.out.print("> ");
        synchronized (barrels) {
            barrels.add(c);
        }
        return bAllCounter;
    }

    public void unsubscribe(String name, BarrelModule_S_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        synchronized (barrels) {
            barrels.remove(c);
        }
    }

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
        String a;

        try (Scanner sc = new Scanner(System.in)) {

            h = new SearchModuleB(tasks, result_pages);

            Registry r = LocateRegistry.createRegistry(7002);
            r.rebind("XPT", h);

            System.out.println("Hello Barrel_Server ready.");

            System.out.println("Search Module - Barrel connection ready.");

            HashMap<Object, Integer> task;
            BarrelModule_S_I barrelM;

            while (true) {
                synchronized (tasks) {
                    //System.out.println(7);
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                            System.out.println("Number of barrels: " + h.barrels.size());
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
