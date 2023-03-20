package RMISearchModule;

import java.rmi.server.*;
import java.rmi.*;

public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I{
    private Thread t1, t2;
    private SearchModuleB sb;
    private SearchModuleC sc;

    public SearchModule() throws RemoteException {
        super();
        sb = new SearchModuleB();
        t1 = new Thread(sb);
        sc = new SearchModuleC();
        t2 = new Thread(sb);
        t1.start();
        t2.start();
    }

    // =======================================================

    public static void main(String[] args) throws RemoteException {
        SearchModule sm = new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}