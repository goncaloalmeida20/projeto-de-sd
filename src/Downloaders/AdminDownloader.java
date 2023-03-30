package Downloaders;


import RMISearchModule.AdminModule_S_I;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;

public class AdminDownloader extends UnicastRemoteObject implements AdminDownloader_S_I, Runnable {
    public static AdminModule_S_I h;
    public static AdminDownloader c;

    AdminDownloader() throws RemoteException {
        super();
    }

    public void ping() throws RemoteException {

    }

    public void run() {

        try{

            h = (AdminModule_S_I) LocateRegistry.getRegistry(7003).lookup("TPX");
            c = new AdminDownloader();
            h.addDownloader((AdminDownloader_S_I) c);
            System.out.println("Storage Barrel Ready");

            while (true) {

            }

        } catch (Exception e) {
            System.out.println("Exception in AdminDownloader2: " + e);
        }
    }
}
