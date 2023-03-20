package IndexStorageBarrels;

import java.rmi.registry.Registry;
import java.util.*;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import classes.Page;

public class SearchServer implements Runnable, SearchServer_S_I{
    public static int PORT0 = 1099;
    public static String arg0 = "search";
    private Thread t;
    private final HashMap<String, ArrayList<Integer>> invertedIndex;
    private final HashMap<Integer, Page> all_pages;

    public SearchServer(HashMap<String, ArrayList<Integer>> invertedIndex, HashMap<Integer, Page> all_pages) throws RemoteException {
        t = new Thread(this);
        t.start();
        this.invertedIndex = invertedIndex;
        this.all_pages = all_pages;
    }

    public ArrayList<Page> search(String[] terms, int n_page) throws RemoteException {
        boolean get_pages = true;
        ArrayList<ArrayList<Integer>> pages_ids = new ArrayList<>();

        // Verify if the inverted index have all the terms
        for (String term : terms) {
            ArrayList<Integer> p = invertedIndex.get(term);
            if (p == null) {
                get_pages = false;
                break;
            }
            pages_ids.add(p);
        }

        if(!get_pages) return null;

        ArrayList<Integer> common = new ArrayList<>(pages_ids.get(0));
        int common_size;

        for(int i = 1; i < all_pages.size(); i++){
            common.retainAll(pages_ids.get(i));
            common_size = common.size();
            if(common_size == 0) break;
            if(common_size > n_page * 10) break;
        }

        common_size = common.size();
        if(common_size == 0) return null;

        ArrayList<Page> ten_pages = new ArrayList<>();
        for (int i = 0; i < common_size && i < 10; i++) {
            ten_pages.add(all_pages.get(common.get(i)));
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

        for (int i = 0; i < all_pages.size(); i++) {
            Page p = all_pages.get(i);
            if(p.links.contains(url)){
                ten_pages.add(p);
                count++;
            }
            if(count == 10) break;
        }

        return ten_pages;
    }

    public void run() {

    }

    public static void main(String[] args) {
        try {
            HashMap<String, ArrayList<Integer>> invertedIndex = new HashMap<>();
            HashMap<Integer, Page> all_pages = new HashMap<>();

            SearchServer searchS = new SearchServer(invertedIndex, all_pages);
            Registry r = LocateRegistry.createRegistry(PORT0);
            r.rebind(arg0, searchS);

            System.out.println("Search Server ready.");
        } catch (RemoteException re) {
            System.out.println("Exception in SearchImpl.main: " + re);
        }
    }
}