package RMISearchModule;

import classes.Page;

import java.io.Serializable;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {
    private Thread t1, t2;
    private SearchModuleB sb;
    private SearchModuleC sc;

    public NavigableMap<Integer, HashMap<Object, Integer>> tasks;
    public ArrayList<Page> result_pages;

    public synchronized void updateResultPages(ArrayList<Page> pages) {
        result_pages.clear();
        result_pages.addAll(pages);
    }

    public synchronized ArrayList<Page> getResultPages(){
        return result_pages;
    }

    public SearchModule() throws RemoteException {
        super();
        tasks = new TreeMap<>();
        result_pages = new ArrayList<>();
        sb = new SearchModuleB(this);
        t1 = new Thread(sb);
        sc = new SearchModuleC(this);
        t2 = new Thread(sc);
        t1.start();
        t2.start();
    }

    // =======================================================

    public static void main(String[] args) throws RemoteException {
        SearchModule sm = new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}