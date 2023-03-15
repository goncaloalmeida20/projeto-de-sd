package IndexStorageBarrels;

import java.rmi.*;
import java.rmi.server.*;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;

import classes.Page;


public class SearchImpl extends UnicastRemoteObject implements SearchIf {
    private static int PORT = 1099;
    private HashMap<String, ArrayList<Integer>> invertedIndex;
    private HashMap<Integer, Page> all_pages = new HashMap<Integer, Page>();

    public SearchImpl(HashMap<String, ArrayList<Integer>> invertedIndex, HashMap<Integer, Page> all_pages) throws RemoteException {
        super();
        this.invertedIndex = invertedIndex;
        this.all_pages = all_pages;
    }

    public ArrayList<Page> search(String[] terms, int n_page) throws RemoteException {
        boolean get_pages = true;
        ArrayList<ArrayList<Integer>> pages_ids = new ArrayList<ArrayList<Integer>>();

        // Verify if the inverted index have all of the terms
        for(int i = 0; i < terms.length; i++){
            ArrayList<Integer> p = invertedIndex.get(terms[i]);
            if(p == null) {
                get_pages = false;
                break;
            }
            pages_ids.add(p);
        }

        if(!get_pages) return null;

        ArrayList<Integer> common = new ArrayList<Integer>(pages_ids.get(0));
        int common_size;

        for(int i = 1; i < all_pages.size(); i++){
            common.retainAll(pages_ids.get(i));
            common_size = common.size();
            if(common_size == 0) break;
            if(common_size > n_page * 10) break;
        }

        common_size = common.size();
        if(common_size == 0) return null;

        ArrayList<Page> ten_pages = new ArrayList<Page>();
        for (int i = 0; i < common_size && i < 10; i++) {
            ten_pages.add(all_pages.get(common.get(i)));
        }

        ArrayList<Page> final_ten_pages = order_pages(ten_pages);

        return final_ten_pages;
    }

    private ArrayList<Page> order_pages(ArrayList<Page> pages){
        ArrayList<Page> pages_ordered = new ArrayList<Page>();

        Collections.sort(pages_ordered, Comparator.comparing(Page::n_links));

        return pages_ordered;
    }

    public ArrayList<Page> search_pages(String url, int n_page) throws RemoteException {
        ArrayList<Page> ten_pages = new ArrayList<Page>();
        int count = 0;

        for (Page p : ten_pages) {
            if(p.links.contains(url)){
                ten_pages.add(p);
                count++;
            }
            if(count == 10) break;
        }

        return ten_pages;
    }

    /*private ArrayList<Page> merge_arraylists_of_pages(ArrayList<Page> one, ArrayList<Page> two){
        ArrayList<Page> merged = new ArrayList<Page>(one);
        for(int i = 0; i < two.size(); i++){
            merged.add(two.get(i));
        }
        return merged;
    }*/
}