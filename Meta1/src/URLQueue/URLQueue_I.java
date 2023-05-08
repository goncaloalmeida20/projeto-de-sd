package URLQueue;

import java.rmi.*;

public interface URLQueue_I extends Remote{
    public boolean addURL(String newURL) throws RemoteException;
    public boolean addURLRecursively(String newURL, int recursion_count) throws RemoteException, InterruptedException;
    public URLItem nextURL() throws RemoteException;
}
