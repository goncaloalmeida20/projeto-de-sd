package RMISearchModule;
import IndexStorageBarrels.BarrelModule;
import IndexStorageBarrels.BarrelModule_S_I;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.ServerNotActiveException;

public interface SearchModuleB_S_I extends Remote {
    void connect(BarrelModule_S_I bm) throws java.rmi.RemoteException;
}


