package RMISearchModule;

import classes.Page;

import java.io.Serializable;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {
    private Thread t1, t2;
    private SearchModule3 sb;
    private SearchModuleC sc;

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    public SearchModule() throws RemoteException {
        super();
        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();
        sb = new SearchModule3(tasks, result_pages);
        t1 = new Thread(sb);
        sc = new SearchModuleC(tasks, result_pages);
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