package RMISearchModule;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;

public class SearchModuleC extends UnicastRemoteObject implements Runnable, SearchModuleC_S_I, Serializable {
    // RMI Client info
    public static int PORT0 = 7004;
    public static String hostname0 = "127.0.0.1";


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

    private void saveServer() {
        try {
            FileOutputStream fileOut = new FileOutputStream("src/databases/serverInfo.ser");
            ObjectOutputStream Objout = new ObjectOutputStream(fileOut);
            Objout.writeObject(SearchModule.sI);

            Objout.close();
            fileOut.close();

            System.out.println("ServerInfo object saved in serverInfo.ser");
        } catch (IOException e) {
            System.out.println("Server save: " + e.getMessage());
        }
    }

    private boolean findClient(String username){
        synchronized (SearchModule.sI.cIList){
            for(ClientInfo cI: SearchModule.sI.cIList){
                if(cI.username.equals(username)) return true;
            }
        }
        return false;
    }

    public synchronized int register(String username, String password, SearchModuleC_S_I s) throws RemoteException {
        boolean exist = findClient(username);
        if (exist){
            return 0; // "Client already exists!"
        } else {
            SearchModule.sI.cAllCounter++;
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.cIList.add(new ClientInfo(SearchModule.sI.cAllCounter, 0, username, password));
            }
            saveServer();
            return SearchModule.sI.cAllCounter; // "Client is now registered!"
        }

    }

    private int verifyLoggedClient(String username, String password, int id){
        int login = 2; // 0 - Already logged in -- 1 - Logged in -- 2 - Invalid credentials
        synchronized (SearchModule.sI.cIList){
            for(ClientInfo cI: SearchModule.sI.cIList){
                if(cI.username.equals(username) && cI.password.equals(password)) {
                    login = cI.logged;
                    break;
                }
            }
        }
        return login;
    }

    public int login(String username, String password, int id) throws RemoteException {
        int logged = verifyLoggedClient(username, password, id);
        if (logged == 0){
            return 0; // "Client already logged on!"
        } else if(logged == 1) {
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.clientInfoById(id).logged = 1;
            }
            saveServer();
            return 1; // "Client is now logged on!"
        } else{
            return 2; // "Invalid credentials"
        }
    }

    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, InterruptedException {
        HashMap<Object, Integer> task = new HashMap<>();
        task.put(terms, n_page);
        addTask(1, task);
        synchronized(result_pages) {
            while (!result_pages.containsKey(this)) {
                result_pages.wait();
            }
            ArrayList<Page> res = result_pages.get(this);
            result_pages.remove(this);
            return res;
        }
    }

    public ArrayList<Page> searchPages(String url, int n_page, int id, boolean logged) throws RemoteException, InterruptedException {
        if (!logged){
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

    //TODO: IT IS NECESSARY TO CREATE A THREAD TO DO THIS
    public void admin() throws RemoteException {

    }

    public int logout(int id) throws RemoteException {
        int logged;
        synchronized (SearchModule.sI.cIList){
            logged = SearchModule.sI.clientInfoById(id).logged;
        }
        System.out.println(logged);
        if (logged == 0){
            return 0; // "Client is not logged on, so it cannot log out!"
        } else {
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.clientInfoById(id).logged = 0;
                saveServer();
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