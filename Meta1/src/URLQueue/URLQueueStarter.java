package URLQueue;

import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class URLQueueStarter {
    public static final int URLQUEUE_PORT = 7010;
    public static final String URLQUEUE_NAME = "URL_QUEUE";
    public static void main(String[] args) {
        try{
            URLQueue uq = new URLQueue();
            Registry r = LocateRegistry.createRegistry(URLQUEUE_PORT);
            r.rebind(URLQUEUE_NAME, uq);
        }
        catch(Exception e){
            System.out.println("URL Queue exception: " + e.getMessage());
        }
    }
}
