package RMISearchModule;
import IndexStorageBarrels.BarrelModule;

import java.io.Serializable;
import java.rmi.*;
import java.rmi.server.ServerNotActiveException;

public interface SearchModuleB_S_I extends Remote, Serializable {
    void connect(BarrelModule bm) throws RemoteException, ServerNotActiveException;
}


