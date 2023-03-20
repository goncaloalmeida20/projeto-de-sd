package RMISearchModule;
import IndexStorageBarrels.BarrelModule;

import java.rmi.*;

public interface SearchModuleB_S_I extends Remote{
    void connect(BarrelModule bm) throws RemoteException;
}


