package IndexStorageBarrels;

import classes.Page;

import java.rmi.Remote;
import java.util.ArrayList;

public interface BarrelModule_S_I extends Remote {
    public void print_on_client(String s) throws java.rmi.RemoteException;

    ArrayList<Page> search(String[] terms, int n_page) throws java.rmi.RemoteException;
    ArrayList<Page> search_pages(String url, int n_page) throws java.rmi.RemoteException;
}
