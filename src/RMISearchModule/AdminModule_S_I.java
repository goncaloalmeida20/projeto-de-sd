package RMISearchModule;

import Downloaders.AdminDownloader_S_I;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdminModule_S_I extends Remote {
    void addDownloader(AdminDownloader_S_I adminDownloader) throws RemoteException;
}
