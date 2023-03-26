package IndexStorageBarrels;

import RMISearchModule.SearchModuleB_S_I;
import classes.Page;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Comparator;

public class BarrelModule extends UnicastRemoteObject implements BarrelModule_S_I,Runnable {
    public static SearchModuleB_S_I h;
    public static BarrelModule c;

    private int id;

    public BarrelModule() throws RemoteException {
        super();
    }

    /**
     * Realiza diferentes operacoes tendo em conta mensagem recevida pelo servidor
     */
    public void print_on_client(String s) throws RemoteException {

        //"start_search"
        System.out.println("recebi o pedido.");
        h.print_on_server("Esta aqui a mensagem pedida.", c);

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

    /**
     * Inicia conexao com servidor e pergunta ao cliente o que ele deseja fazer, realiza diferentes operacoes tendo em conta a escolha do cliente
     *
     */
    @Override
    public void run() {

        try{

            h = (SearchModuleB_S_I) LocateRegistry.getRegistry(7002).lookup("XPT");
            c = new BarrelModule();
            id = h.subscribe((BarrelModule_S_I) c);
            System.out.println("Storage Barrel Ready");

            while (true) {

            }

        } catch (Exception e) {
            System.out.println("Exception in main: " + e);
        }
    }
}
