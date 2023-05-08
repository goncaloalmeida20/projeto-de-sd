package Downloaders;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AdminDownloader_S_I extends Remote {
    void ping() throws RemoteException;

    int getId() throws RemoteException;
}
