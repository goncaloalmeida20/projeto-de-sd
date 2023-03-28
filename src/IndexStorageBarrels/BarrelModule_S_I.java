package IndexStorageBarrels;

import RMISearchModule.SearchModuleB_S_I;
import classes.Page;

import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.sql.SQLException;
import java.util.ArrayList;

public interface BarrelModule_S_I extends Remote {
    ArrayList<Page> search(String[] terms, int n_page) throws java.rmi.RemoteException;
    ArrayList<Page> search_pages(String url, int n_page) throws java.rmi.RemoteException, SQLException;
    int getId() throws java.rmi.RemoteException;
}
