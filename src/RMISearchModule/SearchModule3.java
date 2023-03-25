package RMISearchModule;

import IndexStorageBarrels.BarrelModule3_S_I;
import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class SearchModule3 extends UnicastRemoteObject implements SearchModule3_S_I, Runnable {
    public ArrayList<BarrelModule3_S_I> clients;
    public SearchModule3 h;

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;



    public SearchModule3(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;
        clients = new ArrayList<BarrelModule3_S_I>();
    }

    public void print_on_client(String s) throws RemoteException{
        System.out.println(s);
    }

    public void print_on_server(String s , BarrelModule3_S_I c) throws RemoteException {

        System.out.println("> " + s);

        /**
         * Inicia conexao entre search module e storage barrel
         */
        if(s.equals("search")){

            try{
                SearchModule3_S_I server = (SearchModule3_S_I) LocateRegistry.getRegistry(7002).lookup("XPTO");
                server.subscribe("Barrels Server", (BarrelModule3_S_I) h);
                synchronized (clients) {
                    Random rand = new Random();
                    int rand_int = rand.nextInt(clients.size() - 1);

                    System.out.println(clients.size() + " |||| " + rand_int);

                    BarrelModule3_S_I client = clients.get(rand_int);
                    client.print_on_client("search_start");

                    server.print_on_server("Enviei uma mensagem|", (BarrelModule3_S_I) h);
                    server.unsubscribe("Barrels Server", (BarrelModule3_S_I) h);
                }
            }catch(Exception re){
                System.out.println("Error");
            }

        }


    }


    public void subscribe(String name, BarrelModule3_S_I c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        synchronized (clients) {
            clients.add(c);
        }
    }

    public void unsubscribe(String name, BarrelModule3_S_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        synchronized (clients) {
            clients.remove(c);
        }
    }

    private BarrelModule3_S_I getRandomBarrelModule() {
        synchronized (h.clients) {
            if (h.clients.isEmpty()) {
                return null; // No active clients
            }
            int randomIndex = (int) (Math.random() * h.clients.size());
            Iterator<BarrelModule3_S_I> iter = h.clients.iterator();
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

            h = new SearchModule3(tasks, result_pages);

            Registry r = LocateRegistry.createRegistry(7002);
            r.rebind("XPT", h);

            System.out.println("Hello Barrel_Server ready.");

            System.out.println("Search Module - Barrel connection ready.");

            HashMap<Object, Integer> task;
            BarrelModule3_S_I barrelM;

            while (true) {
                synchronized (tasks) {
                    //System.out.println(7);
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                            System.out.println("Number of barrels: " + h.clients.size());
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
