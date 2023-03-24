package IndexStorageBarrels;

import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.io.*;

import RMISearchModule.SearchModuleB;
import RMISearchModule.SearchModuleB_S_I;
import classes.Page;

public class BarrelModule implements Runnable, BarrelModule_S_I, Serializable{
    public int id;
    public static SearchModuleB_S_I searchMB;
    public static BarrelModule bm;

    public BarrelModule(int id) throws RemoteException, NotBoundException {
        this.id = id;
    }

    public ArrayList<Page> search(String[] terms, int n_page) throws RemoteException {
        boolean get_pages = true;
        ArrayList<ArrayList<Integer>> pagesIds = new ArrayList<>();

        // Verify if the inverted index have all the terms
        ArrayList<Integer> p;
        for (String term : terms) {
            synchronized (Barrel.invertedIndex){
                p = Barrel.invertedIndex.get(term);
            }
            if (p == null) {
                get_pages = false;
                break;
            }
            pagesIds.add(p);
        }

        if(!get_pages) return null;

        ArrayList<Integer> common = new ArrayList<>(pagesIds.get(0));
        int commonSize, allPagesSize;

        synchronized (Barrel.all_pages){
            allPagesSize = Barrel.all_pages.size();
        }

        for(int i = 1; i < allPagesSize; i++){
            common.retainAll(pagesIds.get(i));
            commonSize = common.size();
            if(commonSize == 0) break;
            if(commonSize > n_page * 10) break;
        }

        commonSize = common.size();
        if(commonSize == 0) return null;

        ArrayList<Page> ten_pages = new ArrayList<>();
        Page page;
        for (int i = 0; i < commonSize && i < 10; i++) {
            synchronized (Barrel.all_pages){
                page = Barrel.all_pages.get(common.get(i));
            }
            ten_pages.add(page);
        }

        return order_pages(ten_pages);
    }

    private ArrayList<Page> order_pages(ArrayList<Page> pages){
        ArrayList<Page> pages_ordered = new ArrayList<>(pages);

        pages_ordered.sort(Comparator.comparing(Page::n_links));

        return pages_ordered;
    }

    public ArrayList<Page> search_pages(String url, int n_page) throws RemoteException {
        ArrayList<Page> ten_pages = new ArrayList<>();
        int count = 0;

        int allPagesSize;
        Page p;
        boolean contains;
        synchronized (Barrel.all_pages){
            allPagesSize = Barrel.all_pages.size();
        }
        for (int i = 0; i < allPagesSize; i++) {
            synchronized (Barrel.all_pages){
                p = Barrel.all_pages.get(i);
            }
            synchronized (p.links){
                contains = p.links.contains(url);
            }
            if(contains){
                ten_pages.add(p);
                count++;
            }
            if(count == 10) break;
        }

        return ten_pages;
    }

    public void run() {
        try{
            Registry r = LocateRegistry.getRegistry(SearchModuleB.PORT1);
            searchMB = (SearchModuleB_S_I) r.lookup(SearchModuleB.hostname1);
            bm = new BarrelModule(this.id);
            searchMB.connect((BarrelModule_S_I) bm);
        } catch (NotBoundException | RemoteException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Search Server ready.");

        while(true){

        }
    }
}