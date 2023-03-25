package RMISearchModule;

import IndexStorageBarrels.BarrelModule2_S_I;

import java.rmi.Remote;

public interface SearchModule2_S_I extends Remote {
    public void print_on_server(String s, BarrelModule2_S_I c) throws java.rmi.RemoteException;
    public void subscribe(String name, BarrelModule2_S_I c) throws java.rmi.RemoteException;
}
