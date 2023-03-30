package Downloaders;


import RMISearchModule.AdminModule_S_I;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;


/**
 The AdminDownloader class is used to connect to the AdminModule_S_I remote object to add itself as a downloader
 */
public class AdminDownloader extends UnicastRemoteObject implements AdminDownloader_S_I, Runnable {
    public static AdminModule_S_I h;
    public static AdminDownloader c;

    private final int id;

    /**
     * Constructor for AdminDownloader class that initializes its id
     * @param id The id of the AdminDownloader object
     * @throws RemoteException If a RemoteException occurs
     */
    AdminDownloader(int id) throws RemoteException {
        super();
        this.id = id;
    }

    /**
     * Function just to test the connection between AdminModule and AdminDownloader
     * @throws RemoteException If a RemoteException occurs (AdminDownloader cannot be connected by AdminModule)
     */
    public void ping() throws RemoteException {

    }

    /**
     * Get the id of the AdminDownloader object
     * @return The id of the AdminDownloader object
     * @throws RemoteException If a RemoteException occurs
     */
    public int getId() throws RemoteException{
        return id;
    }


    /**
     * Implementation of the run method that has the sole purpose
     * to keep the thread alive and connect to the AdminModule (The server)
     */
    public void run() {

        try{

            h = (AdminModule_S_I) LocateRegistry.getRegistry(7003).lookup("TPX");
            c = new AdminDownloader(id);
            h.addDownloader((AdminDownloader_S_I) c);
            System.out.println("Admin Downloader Ready");

            while (true) {

            }

        } catch (Exception e) {
            System.out.println("Exception in AdminDownloader: " + e);
        }
    }
}
