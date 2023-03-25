package RMISearchModule;

import IndexStorageBarrels.BarrelModule3_S_I;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchModule3_S_I extends Remote {
    public void print_on_server(String s , BarrelModule3_S_I client) throws java.rmi.RemoteException;
    public void subscribe(String name, BarrelModule3_S_I client) throws RemoteException;
    public void unsubscribe(String name, BarrelModule3_S_I client) throws RemoteException;
}
