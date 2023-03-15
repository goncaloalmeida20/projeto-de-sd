package URLQueue;

import java.rmi.*;

public interface URLQueue_I extends Remote{
    public boolean addURL(String newURL) throws RemoteException;
    public boolean addURL(String newURL, boolean update_if_exists) throws RemoteException;
    public String nextURL() throws RemoteException;
}
