package RMISearchModule;

import java.rmi.registry.LocateRegistry;
import java.util.*;
import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import IndexStorageBarrels.SearchServer_S_I;
import IndexStorageBarrels.SearchServer;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I{
    public final Map<Integer, Integer> clients_log; // 0 - off 1 - on (login)
    public final Map<Integer, String[]> clients_info;

    public static int PORT = 7000;
    public static String hostname = "127.0.0.1";

    private int cAllCounter = 0;

    public SearchModule() throws RemoteException {
        super();
        clients_log = Collections.synchronizedMap(new HashMap<>());
        clients_info = Collections.synchronizedMap(new HashMap<>());
    }

    public synchronized int connectSM() throws RemoteException {
        cAllCounter++;
        return cAllCounter;
    }

    public String login(String username, String password, int id) throws RemoteException {
        // System.out.println(c);
        // clients_log.forEach((key, value)-> System.out.println(key + " = " + value));
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        // System.out.println("logged: " + logged);
        if (logged == 1){
            return "Client already logged on! Username and password not changed.";
        } else {
            synchronized (clients_log){
                clients_log.put(id, 1);
            }
            synchronized (clients_info){
                clients_info.put(id, new String[]{username, password});
            }
            // clients.forEach((key, value)-> System.out.println(key + " = " + value));
            return "Client is now logged on!";
        }
    }

    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, NotBoundException {
        SearchServer_S_I si = (SearchServer_S_I) LocateRegistry.getRegistry(SearchServer.PORT0).lookup(SearchServer.arg0);
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
            SearchServer_S_I si = (SearchServer_S_I) LocateRegistry.getRegistry(SearchServer.PORT0).lookup(SearchServer.arg0);
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
            clients_log.remove(id);
            return "Client is now logged off!";
        }
    }

    // =======================================================

    public static void main(String[] args){
        try {
            SearchModule searchM = new SearchModule();
            Registry r = LocateRegistry.createRegistry(PORT);
            r.rebind(hostname, searchM);

            System.out.println("Search Module ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}