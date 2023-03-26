package RMISearchModule;

import java.io.Serializable;
import java.rmi.registry.LocateRegistry;
import java.util.*;
import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;
import java.util.concurrent.atomic.AtomicBoolean;

public class SearchModuleC extends UnicastRemoteObject implements Runnable, SearchModuleC_S_I, Serializable {

    public static final Map<Integer, Integer> clients_log = Collections.synchronizedMap(new HashMap<>()); // 0 - off 1 - on (login)
    public static final Map<Integer, String[]> clients_info = Collections.synchronizedMap(new HashMap<>()); // 0 - off 1 - on (login)

    // RMI Client info
    public static int PORT0 = 7004;
    public static String hostname0 = "127.0.0.1";

    private int cAllCounter = 0;

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    public SearchModuleC(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;
    }

    private void addTask(int type, HashMap<Object, Integer> task) throws RemoteException {
        synchronized (tasks){
            HashMap<SearchModuleC, Integer> cliendThreadAndTaskType = new HashMap<>();
            cliendThreadAndTaskType.put(this, type);
            tasks.put(cliendThreadAndTaskType, task);
            tasks.notify();
        }
    }

    private boolean findClient(String username){
        synchronized (clients_info){
            for (Map.Entry<Integer, String[]> set :
                    clients_info.entrySet()) {
                if(set.getValue()[0].equals(username)) return true;
            }
        }
        return false;
    }

    public synchronized int register(String username, String password) throws RemoteException {
        boolean exist = findClient(username);
        if (exist){
            return 0; // "Client already exists!"
        } else {
            cAllCounter++;
            synchronized (clients_info){
                clients_info.put(cAllCounter , new String[]{username, password});
            }
            synchronized (clients_log){
                clients_log.put(cAllCounter , 0);
            }
            return cAllCounter; // "Client is now registered!"
        }

    }

    private int verifyLoggedClient(String username, String password, int id){
        int login = 2; // 0 - Already logged in -- 1 - Logged in -- 2 - Invalid credentials
        synchronized (clients_info){
            for (Map.Entry<Integer, String[]> set :
                    clients_info.entrySet()) {
                if (set.getValue()[0].equals(username) && set.getValue()[1].equals(password)) {
                    login = 1;
                    break;
                }
            }
        }
        if(login == 2) return 2;
        synchronized (clients_log){
            boolean info = clients_log.get(id) == null;
            if(!info) return 0;
        }
        return 1;
    }

    public int login(String username, String password, int id) throws RemoteException {
        int logged = verifyLoggedClient(username, password, id);
        if (logged == 0){
            return 0; // "Client already logged on!"
        } else if(logged == 1) {
            synchronized (clients_log){
                clients_log.put(cAllCounter, 1);
            }
            return 1; // "Client is now logged on!"
        } else{
            return 2; // "Invalid credentials"
        }
    }

    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, NotBoundException, InterruptedException {
        HashMap<Object, Integer> task = new HashMap<>();
        task.put(terms, n_page);
        addTask(1, task);
        System.out.println(4);
        synchronized(result_pages) {
            System.out.println(5);
            while (!result_pages.containsKey(this)) {
                result_pages.wait();
                System.out.println(3);
            }
            System.out.println(6);
            ArrayList<Page> res = result_pages.get(this);
            result_pages.remove(this);
            return res;
        }
    }

    public ArrayList<Page> searchPages(String url, int n_page, int id) throws RemoteException, NotBoundException, InterruptedException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 0){
            return null;
        } else {
            HashMap<Object, Integer> task = new HashMap<>();
            task.put(url, n_page);
            addTask(2, task);
            synchronized(result_pages) {
                while (!result_pages.containsKey(this)) {
                    result_pages.wait();
                }
                ArrayList<Page> res = result_pages.get(this);
                result_pages.remove(this);
                return res;
            }
        }
    }

    // TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public void admin() throws RemoteException {

    }

    public int logout(int id) throws RemoteException {
        int logged;
        synchronized (clients_log){
            logged = clients_log.get(id) == null ? 0 : 1;
        }
        if (logged == 0){
            return 0; // "Client is not logged on, so it cannot log out!"
        } else {
            synchronized (clients_log){
                clients_log.remove(id);
            }
            return 1; // "Client is now logged off!"
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