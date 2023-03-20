package RMISearchModule;


import classes.Page;

import java.rmi.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public interface SearchModule_S_I extends Remote{
    void addTask(int type, HashMap<Object, Integer> task) throws RemoteException;
    Map.Entry<Integer, HashMap<Object, Integer>> nextTask() throws RemoteException;
}
