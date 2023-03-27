package RMISearchModule;

import IndexStorageBarrels.BarrelModule_S_I;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SearchModuleB_S_I extends Remote {
    public int connect(BarrelModule_S_I barrel) throws RemoteException;
}
