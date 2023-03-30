package RMISearchModule;

import classes.Page;

import java.rmi.*;
import java.rmi.server.ServerNotActiveException;
import java.util.ArrayList;
import java.util.Map;

public interface SearchModuleC_S_I extends Remote{
    //void printOnServer(ClientInterface_C_I c) throws RemoteException;
    int register(String username, String password) throws java.rmi.RemoteException;
    int login(String username, String password, int id) throws java.rmi.RemoteException, ServerNotActiveException;
    void indexUrl(String url) throws java.rmi.RemoteException, NotBoundException;
    ArrayList<Page> search(int termCount, String[] terms, int n_page) throws java.rmi.RemoteException, NotBoundException, InterruptedException;
    ArrayList<Page> searchPages(String url, int n_page, int id, boolean logged) throws java.rmi.RemoteException, NotBoundException, ServerNotActiveException, InterruptedException;
    Map<Integer, Integer> admin() throws java.rmi.RemoteException;
    int logout(int id) throws java.rmi.RemoteException, ServerNotActiveException;
}
