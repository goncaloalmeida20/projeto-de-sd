package RMISearchModule;

import java.rmi.registry.LocateRegistry;
import java.util.*;
import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import IndexStorageBarrels.SearchIf;
import IndexStorageBarrels.SearchServer;
import RMIClient.ClientInterface;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I{
    static ClientInterface clientI;
    public HashMap<String, Integer> clients_log;
    public HashMap<String, String[]> clients_info;

    public static int PORT = 7000;
    public static String hostname = "127.0.0.1";

    public SearchModule() throws RemoteException {
        super();
        clients_log = new HashMap<String, Integer>();
        clients_info = new HashMap<String, String[]>();
    }

    public String login(String username, String password) throws RemoteException, ServerNotActiveException {
        String c = RemoteServer.getClientHost();
        //System.out.println(c);
        //clients_log.forEach((key, value)-> System.out.println(key + " = " + value));
        int logged = clients_log.get(c) == null ? 0 : 1;
        // System.out.println("logged: " + logged);
        try {
            Thread.sleep(5000);
            System.out.println(username == null);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if (logged == 1){
            return "Client already logged on! Username and password not changed.";
        } else {
            clientI = new ClientInterface(username, password);
            clients_log.put(c, 1);
            clients_info.put(c, new String[]{username, password});
            //clients.forEach((key, value)-> System.out.println(key + " = " + value));
            return "Client is now logged on!";
        }
    }

    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    // TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, NotBoundException {
        SearchIf si = (SearchIf) LocateRegistry.getRegistry(SearchServer.PORT0).lookup(SearchServer.arg0);
        return si.search(terms, n_page);
    }

    // TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public ArrayList<Page> searchPages(String url, int n_page) throws RemoteException, NotBoundException {
        SearchIf si = (SearchIf) LocateRegistry.getRegistry(SearchServer.PORT0).lookup(SearchServer.arg0);
        return si.search_pages(url, n_page);
    }

    // TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public void admin() throws RemoteException {

    }

    public String logout() throws RemoteException, ServerNotActiveException {
        String c = RemoteServer.getClientHost();
        int logged = clients_log.get(c) == null ? 0 : 1;
        if (logged == 0){
            return "Client is not logged on, so it cannot loggout!";
        } else {
            clients_log.remove(c);
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