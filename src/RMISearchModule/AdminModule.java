package RMISearchModule;

import Downloaders.AdminDownloader_S_I;
import IndexStorageBarrels.BarrelModule_S_I;

import java.rmi.*;
        import java.rmi.server.*;
        import java.rmi.registry.LocateRegistry;
        import java.rmi.registry.Registry;
        import java.util.ArrayList;
        import java.util.Scanner;
import java.util.*;


public class AdminModule extends UnicastRemoteObject implements AdminModule_S_I, Runnable {
    public AdminModule h;


    public AdminModule() throws RemoteException {
        super();
    }

    public void addDownloader(AdminDownloader_S_I adminDownloader) throws RemoteException{
        synchronized (SearchModule.sI.adminDownloaders){
            SearchModule.sI.adminDownloaders.add(adminDownloader);
        }
    }

    private static void renovateBarrels(List<Integer> notActiveBarrels){
        synchronized (SearchModule.sI.barrels) {
            int size = SearchModule.sI.barrels.size();
            for (int i = 0; i < size; i++) {
                if(notActiveBarrels.contains(i)) {
                    SearchModule.sI.barrels.remove(i);
                    i--;
                }
            }
        }
    }

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

    private static void renovateDownloaders(List<Integer> notActiveDownloaders){
        synchronized (SearchModule.sI.adminDownloaders) {
            int size = SearchModule.sI.adminDownloaders.size();
            for (int i = 0; i < size; i++) {
                if(notActiveDownloaders.contains(i)) {
                    SearchModule.sI.adminDownloaders.remove(i);
                    i--;
                }
            }
        }
    }

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

    public static Map<Integer, Integer> getActiveDownloaderAndBarrels(){
        int activeBarrels = pingBarrels(), activeDownloaders = pingDownloaders();
        Map<Integer, Integer> active = Collections.synchronizedMap(new HashMap<>());
        active.put(activeBarrels, activeDownloaders);
        return active;
    }

    // =======================================================
    @Override
    public void run() {
        try {

            h = new AdminModule();

            Registry r = LocateRegistry.createRegistry(7003);
            r.rebind("TPX", h);

            System.out.println("Hello Barrel_Server ready.");

            while (true) {

            }
        } catch (Exception re) {
            System.out.println("Exception in HelloImpl.main: " + re);
        }
    }
}