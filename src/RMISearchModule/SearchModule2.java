package RMISearchModule;

import IndexStorageBarrels.BarrelModule2_S_I;
import IndexStorageBarrels.BarrelModule_S_I;
import classes.Page;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Scanner;
import  java.util.Random;
import java.util.*;

public class SearchModule2 implements SearchModule2_S_I, Runnable, Serializable {

    public ArrayList<BarrelModule2_S_I> clients;
    public HashMap<String, String> registed_users = new HashMap<String, String>();

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    public SearchModule2 h;


    public SearchModule2(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) throws RemoteException {
        super();
        clients = new ArrayList<BarrelModule2_S_I>();
        tasks = t;
        result_pages = p;
    }

    private BarrelModule2_S_I getRandomBarrelModule() {
        synchronized (clients) {
            if (clients.isEmpty()) {
                return null; // No active clients
            }
            int randomIndex = (int) (Math.random() * clients.size());
            Iterator<BarrelModule2_S_I> iter = clients.iterator();
            for (int i = 0; i < randomIndex; i++) {
                iter.next();
            }
            return iter.next();
        }
    }

    public void print_on_server(String s , BarrelModule2_S_I c) throws RemoteException {

        System.out.println("> " + s);

        /**
         * Inicia conexao entre search module e storage barrel
         */
        if(s.equals("search")){

            try{
                SearchModule2_S_I server = (SearchModule2_S_I) LocateRegistry.getRegistry(7000).lookup("XPTO");
                server.subscribe("Barrels Server", (BarrelModule2_S_I) h);

                Random rand = new Random();
                int rand_int = rand.nextInt(clients.size()-1);

                System.out.println(clients.size() + " |||| " + rand_int );

                BarrelModule2_S_I client = clients.get(rand_int);
                //client.print_on_client("search_start");

                server.print_on_server("Enviei uma mensagem|", (BarrelModule2_S_I) client);
                //server.unsubscribe("Barrels Server", (BarrelModule2_S_I) h);
            }catch(Exception re){
                System.out.println("Error");
            }

        }


    }

    public void subscribe(String name, BarrelModule2_S_I c) throws RemoteException {
        System.out.println("Subscribing " + name);
        System.out.print("> ");
        clients.add(c);
    }

    public void unsubscribe(String name, BarrelModule2_S_I c) throws RemoteException {
        System.out.println("Unsubscribing " + name);
        System.out.print("> ");
        clients.remove(c);
    }

    // =======================================================
    public void run() {
        String a;

        try (Scanner sc = new Scanner(System.in)) {

            h = new SearchModule2(tasks, result_pages);

            Registry r = LocateRegistry.createRegistry(7001);
            r.rebind("XPT", h);

            System.out.println("Hello Barrel_Server ready.");

            HashMap<Object, Integer> task;
            BarrelModule2_S_I barrelM;

            /*while (true) {
                synchronized (tasks) {
                    //System.out.println(7);
                    while (tasks.isEmpty()) {
                        try {
                            tasks.wait();
                            System.out.println("Number of barrels: " + clients.size());
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
            }*/
            while (true) {

                System.out.print(">");
                a = sc.nextLine();
                System.out.println("Clients size: " + h.clients.size());
                for (BarrelModule2_S_I client : h.clients) {
                    //client.print_on_client(a);
                }

            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
