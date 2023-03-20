package RMISearchModule;

import java.rmi.registry.LocateRegistry;
import java.util.*;
import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import IndexStorageBarrels.BarrelModule_S_I;
import IndexStorageBarrels.BarrelModule;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;

public class SearchModuleC extends UnicastRemoteObject implements Runnable, SearchModuleC_S_I{
    public final Map<Integer, Integer> clients_log; // 0 - off 1 - on (login)
    public final Map<Integer, String[]> clients_info;

    // RMI Client info
    public static int PORT0 = 7000;
    public static String hostname0 = "127.0.0.1";

    private int cAllCounter = 0;

    private final SearchModule father;

    public SearchModuleC(SearchModule f) throws RemoteException {
        super();
        clients_log = Collections.synchronizedMap(new HashMap<>());
        clients_info = Collections.synchronizedMap(new HashMap<>());
        father = f;
    }

    public synchronized int connectSM() throws RemoteException {
        cAllCounter++;
        return cAllCounter;
    }

    public String login(String username, String password, int id) throws RemoteException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 1){
            return "Client already logged on! Username and password not changed.";
        } else {
            synchronized (clients_log){
                clients_log.put(id, 1);
            }
            synchronized (clients_info){
                clients_info.put(id, new String[]{username, password});
            }
            return "Client is now logged on!";
        }
    }

    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, NotBoundException {
        // TODO: CREATE A WAY TO BARREL WRITE SOMEWHERE
        HashMap<Object, Integer> task = new HashMap<>();
        task.put(terms, n_page);
        father.addTask(1, task);
        return new ArrayList<>();
    }

    public ArrayList<Page> searchPages(String url, int n_page, int id) throws RemoteException, NotBoundException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 0){
            return null;
        } else {
            // TODO: CREATE A WAY TO BARREL WRITE SOMEWHERE
            HashMap<Object, Integer> task = new HashMap<>();
            task.put(url, n_page);
            father.addTask(2, task);
            return new ArrayList<>();
        }
    }

    // TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public void admin() throws RemoteException {

    }

    public String logout(int id) throws RemoteException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 0){
            return "Client is not logged on, so it cannot loggout!";
        } else {
            synchronized (clients_log){
                clients_log.remove(id);
            }
            return "Client is now logged off!";
        }
    }

    // =======================================================

    public void run() {
        try {
            Registry rC = LocateRegistry.createRegistry(PORT0);
            rC.rebind(hostname0, this);

            System.out.println("Search Module - Client connection ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}