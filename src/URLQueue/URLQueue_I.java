package URLQueue;

import java.rmi.*;

public interface URLQueue_I extends Remote{
    public boolean addURL(String newURL) throws RemoteException;
    public boolean replaceURL(String newURL, int recursion_count) throws RemoteException;
    public URLItem nextURL() throws RemoteException;
}
