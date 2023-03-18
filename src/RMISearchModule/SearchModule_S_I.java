package RMISearchModule;

import RMIClient.ClientInterface;
import classes.Page;

import java.rmi.*;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;

public interface SearchModule_S_I extends Remote{
    String login(String username, String password) throws java.rmi.RemoteException, ServerNotActiveException;
    void indexUrl(String url) throws java.rmi.RemoteException, NotBoundException;
    ArrayList<Page> search(int termCount, String[] terms, int n_page) throws java.rmi.RemoteException, NotBoundException;
    ArrayList<Page> searchPages(String url, int n_page) throws java.rmi.RemoteException, NotBoundException;
    void admin() throws java.rmi.RemoteException;
    String logout() throws java.rmi.RemoteException, ServerNotActiveException;
}
