package IndexStorageBarrels;

import java.rmi.*;
import java.util.*;

import classes.Page;

public interface SearchIf extends Remote {
    public ArrayList<Page> search(String[] terms, int n_page) throws java.rmi.RemoteException;
    public ArrayList<Page> search_pages(String url, int n_page) throws java.rmi.RemoteException;
}