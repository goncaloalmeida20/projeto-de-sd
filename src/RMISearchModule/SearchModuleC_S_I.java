package RMISearchModule;

import classes.Page;

import java.rmi.*;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface SearchModuleC_S_I extends Remote{
    int connectSM() throws java.rmi.RemoteException;
    String login(String username, String password, int id) throws java.rmi.RemoteException, ServerNotActiveException;
    void indexUrl(String url) throws java.rmi.RemoteException, NotBoundException;
    ArrayList<Page> search(int termCount, String[] terms, int n_page) throws java.rmi.RemoteException, NotBoundException;
    ArrayList<Page> searchPages(String url, int n_page, int id) throws java.rmi.RemoteException, NotBoundException, ServerNotActiveException;
    void admin() throws java.rmi.RemoteException;
    String logout(int id) throws java.rmi.RemoteException, ServerNotActiveException;
}
