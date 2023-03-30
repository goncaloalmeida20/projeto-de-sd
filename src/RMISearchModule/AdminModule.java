package RMISearchModule;

import Downloaders.AdminDownloader_S_I;
import IndexStorageBarrels.BarrelModule_S_I;

import java.rmi.*;
import java.rmi.server.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.*;


/**
 * This class represents the implementation of the Admin Module RMI interface.
 * It allows the addition of an Admin Downloader to the Search Module's list of downloaders,
 * pinging and renovating the available barrels and downloaders and getting the number of active barrels and active downloaders.
 */
public class AdminModule extends UnicastRemoteObject implements AdminModule_S_I, Runnable {
    public AdminModule h;

    /**
     * Constructor for the AdminModule class.
     * @throws RemoteException if the remote method invocation fails
     */
    public AdminModule() throws RemoteException {
        super();
    }

    /**
     * Method for adding an Admin Downloader to the Search Module's list of downloaders.
     * @param adminDownloader the downloader to be added
     * @throws RemoteException if the remote method invocation fails
     */
    public void addDownloader(AdminDownloader_S_I adminDownloader) throws RemoteException{
        synchronized (SearchModule.sI.adminDownloaders){
            SearchModule.sI.adminDownloaders.add(adminDownloader);
        }
    }

    /**
     * Method for renovating the barrels list by removing the barrels that are not active.
     * @param notActiveBarrels the list of indices of the not active barrels
     */
    private static void renovateBarrels(List<Integer> notActiveBarrels){
        synchronized (SearchModule.sI.barrels) {
            for (int i = 0; i < SearchModule.sI.barrels.size(); i++) {
                if(notActiveBarrels.contains(i)) {
                    SearchModule.sI.barrels.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Method for pinging the available barrels and updating the barrels list by removing the barrels that are not active.
     * @return the number of active barrels
     */
    private static int pingBarrels(){
        int activeBarrels = 0;
        List<Integer> notActiveBarrels = new ArrayList<>();
        int idx = 0;
        synchronized (SearchModule.sI.barrels) {
            for (BarrelModule_S_I barrel : SearchModule.sI.barrels) {
                try {
                    barrel.ping();
                    activeBarrels++;
                } catch (RemoteException e) {
                    notActiveBarrels.add(idx);
                }
                idx++;
            }
        }
        renovateBarrels(notActiveBarrels);
        return activeBarrels;
    }

    /**
     * Method for renovating the downloaders list by removing the downloaders that are not active.
     * @param notActiveDownloaders the list of indices of the not active downloaders
     */
    private static void renovateDownloaders(List<Integer> notActiveDownloaders){
        synchronized (SearchModule.sI.adminDownloaders) {
            for (int i = 0; i < SearchModule.sI.adminDownloaders.size(); i++) {
                if(notActiveDownloaders.contains(i)) {
                    SearchModule.sI.adminDownloaders.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Method for pinging the available downloaders and updating the downloaders list by removing the downloaders that are not active.
     * @return the number of active downloaders
     */
    private static int pingDownloaders(){
        int activeDownloaders = 0;
        List<Integer> notActiveDownloaders = new ArrayList<>();
        int idx = 0;
        synchronized (SearchModule.sI.adminDownloaders) {
            for (AdminDownloader_S_I downloader : SearchModule.sI.adminDownloaders) {
                try {
                    downloader.ping();
                    activeDownloaders++;
                } catch (RemoteException e) {
                    notActiveDownloaders.add(idx);
                }
                idx++;
            }
        }
        renovateDownloaders(notActiveDownloaders);
        return activeDownloaders;
    }

    /**
     * This method returns a map containing the number of active downloaders and barrels
     * @return Map<Integer, Integer> A synchronized map containing the number of active downloaders and barrels
     */
    public static Map<Integer, Integer> getActiveDownloaderAndBarrels(){
        int activeBarrels = pingBarrels(), activeDownloaders = pingDownloaders();
        Map<Integer, Integer> active = Collections.synchronizedMap(new HashMap<>());
        active.put(activeBarrels, activeDownloaders);
        return active;
    }

    // =======================================================

    /**
     * This method runs the Admin Module and binds it to the RMI Registry on port 7003.
     * It also creates an instance of the Admin Module and waits for incoming requests.
     */
    @Override
    public void run() {
        try {

            h = new AdminModule();

            Registry r = LocateRegistry.createRegistry(7003);
            r.rebind("TPX", h);

            System.out.println("Admin Module ready");

            while (true) {

            }
        } catch (Exception re) {
            System.out.println("Exception in Admin Module: " + re);
        }
    }
}