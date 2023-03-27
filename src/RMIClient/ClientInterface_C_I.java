package RMIClient;

import java.rmi.*;

public interface ClientInterface_C_I extends Remote{
    void printOnClient(String s) throws RemoteException;
}
