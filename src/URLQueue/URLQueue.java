package URLQueue;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class URLQueue extends UnicastRemoteObject implements URLQueue_I{
    public final List<URLItem> URLList;
    public List<URLItem> indexedURLs;

    public URLQueue() throws RemoteException{
        super();
        URLList = Collections.synchronizedList(new ArrayList<URLItem>());
        indexedURLs = Collections.synchronizedList(new ArrayList<URLItem>());
    }

    public boolean addURL(String newURL) throws RemoteException{
        URLItem uIt = new URLItem(newURL);
        synchronized(URLList){
            if(URLList.contains(uIt)) return false;
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    public boolean replaceURL(String newURL, int recursion_count) throws RemoteException{
        URLItem uIt = new URLItem(newURL);
        synchronized(URLList){
            int ind = URLList.indexOf(uIt);
            if(ind >= 0){
                uIt = URLList.get(ind);
                uIt.decrease_recursion_count();
            }
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    public URLItem nextURL() throws RemoteException{
        synchronized(URLList){
            try{
                while(URLList.size() == 0)
                    URLList.wait();
                return URLList.remove(URLList.size()-1);
            }
            catch(Exception e){
                System.out.println("URLQueue exception: " + e.getMessage());
            }
        }
        return null;
    }
}