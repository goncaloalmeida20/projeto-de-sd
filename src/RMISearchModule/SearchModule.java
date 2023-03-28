package RMISearchModule;

import classes.Page;

import java.io.Serializable;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;

    public SearchModule() throws RemoteException {
        super();
        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();
        SearchModuleB sb = new SearchModuleB(tasks, result_pages);
        Thread t1 = new Thread(sb);
        SearchModuleC sc = new SearchModuleC(tasks, result_pages);
        Thread t2 = new Thread(sc);
        t1.start();
        t2.start();
    }

    // =======================================================

    public static void main(String[] args) throws RemoteException {
        new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}