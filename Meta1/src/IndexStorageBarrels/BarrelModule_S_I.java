package IndexStorageBarrels;

import classes.Page;

import java.rmi.Remote;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public interface BarrelModule_S_I extends Remote {
    ArrayList<Page> search(String[] terms, int n_page) throws java.rmi.RemoteException;
    ArrayList<Page> search_pages(String url, int n_page) throws java.rmi.RemoteException, SQLException;
    int getId() throws java.rmi.RemoteException;
    void ping() throws java.rmi.RemoteException;
    List<HashMap<Integer, String>> getTopTenSearches() throws java.rmi.RemoteException, SQLException;
}
