package URLQueue;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class URLQueue extends UnicastRemoteObject implements URLQueue_I{
    public List<URLItem> URLList;

    public URLQueue() throws RemoteException{
        super();
        URLList = Collections.synchronizedList(new ArrayList<URLItem>());
    }

    public boolean addURL(String newURL) throws RemoteException{
        URLItem uIt = new URLItem(newURL);
        //TODO: check if url exists in barrels
        synchronized(URLList){
            if(URLList.contains(uIt)) return false;
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    public boolean addURL(String newURL, boolean update_if_exists) throws RemoteException{
        URLItem uIt = new URLItem(newURL, update_if_exists);
        if(URLList.contains(uIt)) return false;
        //TODO: check if url exists in barrels
        synchronized(URLList){
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    public String nextURL() throws RemoteException{
        synchronized(URLList){
            try{
                while(URLList.size() == 0)
                    URLList.wait();
                return (URLList.remove(URLList.size()-1)).url;
            }
            catch(Exception e){
                System.out.println("URLQueue exception: " + e.getMessage());
            }
        }
        return null;
    }
}