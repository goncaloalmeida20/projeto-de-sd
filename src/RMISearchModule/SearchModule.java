package RMISearchModule;

import java.rmi.server.*;
import java.rmi.*;
import java.util.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I{
    private Thread t1, t2;
    private SearchModuleB sb;
    private SearchModuleC sc;

    private final NavigableMap<Integer, HashMap<Object, Integer>> tasks;

    public SearchModule() throws RemoteException {
        super();
        tasks = new TreeMap<>();
        sb = new SearchModuleB(this);
        t1 = new Thread(sb);
        sc = new SearchModuleC(this);
        t2 = new Thread(sb);
        t1.start();
        t2.start();
    }

    public void addTask(int type, HashMap<Object, Integer> task) throws RemoteException {
        synchronized (tasks){
            tasks.put(type, task);
            tasks.notify();
        }
    }

    public Map.Entry<Integer, HashMap<Object, Integer>> nextTask() throws RemoteException{
        synchronized(tasks){
            try{
                while(tasks.size() == 0)
                    tasks.wait();
                return tasks.lastEntry();
            }
            catch(Exception e){
                System.out.println("Tasks exception: " + e.getMessage());
            }
        }
        return null;
    }

    // =======================================================

    public static void main(String[] args) throws RemoteException {
        SearchModule sm = new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}