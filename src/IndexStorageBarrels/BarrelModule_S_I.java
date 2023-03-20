package IndexStorageBarrels;

import java.io.Serializable;
import java.rmi.*;
import java.util.*;

import classes.Page;

public interface BarrelModule_S_I extends Remote, Serializable {
    ArrayList<Page> search(String[] terms, int n_page) throws java.rmi.RemoteException;
    ArrayList<Page> search_pages(String url, int n_page) throws java.rmi.RemoteException;
}