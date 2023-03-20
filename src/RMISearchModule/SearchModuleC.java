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

    public SearchModuleC() throws RemoteException {
        super();
        clients_log = Collections.synchronizedMap(new HashMap<>());
        clients_info = Collections.synchronizedMap(new HashMap<>());
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
        BarrelModule_S_I si = (BarrelModule_S_I) LocateRegistry.getRegistry(BarrelModule.PORT0).lookup(BarrelModule.arg0);
        return si.search(terms, n_page);
    }

    public ArrayList<Page> searchPages(String url, int n_page, int id) throws RemoteException, NotBoundException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 0){
            return null;
        } else {
            BarrelModule_S_I si = (BarrelModule_S_I) LocateRegistry.getRegistry(BarrelModule.PORT0).lookup(BarrelModule.arg0);
            return si.search_pages(url, n_page);
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
            SearchModule searchClient = new SearchModule();
            Registry rC = LocateRegistry.createRegistry(PORT0);
            rC.rebind(hostname0, searchClient);

            System.out.println("Search Module - Client connection ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}