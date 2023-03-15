package IndexStorageBarrels;

import classes.Page;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;


public class SearchImpl extends UnicastRemoteObject implements SearchIf {
    private final HashMap<String, ArrayList<Integer>> invertedIndex;
    private final HashMap<Integer, Page> all_pages;

    public SearchImpl(HashMap<String, ArrayList<Integer>> invertedIndex, HashMap<Integer, Page> all_pages) throws RemoteException {
        super();
        this.invertedIndex = invertedIndex;
        this.all_pages = all_pages;
    }

    public ArrayList<Page> search(String[] terms, int n_page) throws RemoteException {
        boolean get_pages = true;
        ArrayList<ArrayList<Integer>> pages_ids = new ArrayList<>();

        // Verify if the inverted index have all of the terms
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
}