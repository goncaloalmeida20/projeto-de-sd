package URLQueue;

import java.io.*;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.*;

public class URLQueue extends UnicastRemoteObject implements URLQueue_I{
    public final List<URLItem> URLList;
    public final Object indexedURLsLock = new Object();

    public URLQueue() throws RemoteException{
        super();
        URLList = Collections.synchronizedList(new ArrayList<URLItem>());
    }

    public boolean tryAddUrlToIndexed(String url){
        File f = new File("urlQueue_already_indexed.txt");
        if(f.exists()){
            try(BufferedReader br = new BufferedReader(new FileReader(f))){
                String line;
                while((line = br.readLine()) != null){
                    if(line.equals(url)){
                        return false;
                    }
                }
            }catch(Exception e){
                System.out.println("Error opening " + f.getName());
                return false;
            }
            try(BufferedWriter bw = new BufferedWriter(new FileWriter(f, true))){
                bw.write(url + "\n");
                return true;
            }catch(Exception e){
                System.out.println("Error writing to " + f.getName());
                return true;
            }
        }
        try(BufferedWriter bw = new BufferedWriter(new FileWriter(f))){
            bw.write(url + "\n");
            return true;
        }catch(Exception e){
            System.out.println("Error writing to " + f.getName());
            return false;
        }
    }

    /**
     * Add URL to the queue
     * @param newURL URL to add to the list
     * @return true if the URL was inserted; false otherwise
     * @throws RemoteException
     */
    public boolean addURL(String newURL) throws RemoteException{
        URLItem uIt = new URLItem(newURL);
        synchronized(URLList){
            if(URLList.contains(uIt)) return false;

            synchronized (indexedURLsLock){
                tryAddUrlToIndexed(uIt.url);
            }
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    /**
     * Add URL to the queue with recursion count
     * @param newURL URL to add to the list
     * @param recursion_count current recursion count of the insertion
     * @return true if the URL was inserted; false otherwise
     * @throws RemoteException
     */
    public boolean addURLRecursively(String newURL, int recursion_count) throws RemoteException {
        URLItem uIt = new URLItem(newURL, recursion_count);
        synchronized (indexedURLsLock){
            if(!tryAddUrlToIndexed(uIt.url)) return false;
        }
        //System.out.println("11111111111111111111111111111111111111111111111");
        synchronized(URLList){
            //Thread.sleep(5000);
            //System.out.println("22222222222222222222222222222222222222222222222");
            if(URLList.contains(uIt)) return false;

            synchronized (indexedURLsLock){
                tryAddUrlToIndexed(uIt.url);
            }
            URLList.add(uIt);
            URLList.notify();
        }
        return true;
    }

    /**
     * waits and retrieves the next URL from the URLQueue
     * @return the next URL from the URLQueue
     * @throws RemoteException
     */
    public URLItem nextURL() throws RemoteException{
        synchronized(URLList){
            try{
                while(URLList.size() == 0)
                    URLList.wait();
                return URLList.remove(0);
            }
            catch(Exception e){
                System.out.println("URLQueue exception: " + e.getMessage());
            }
        }
        return null;
    }
}