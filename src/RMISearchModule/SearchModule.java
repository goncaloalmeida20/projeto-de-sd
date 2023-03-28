package RMISearchModule;

import classes.Page;

import java.io.File;
import java.io.Serializable;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;



public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public static Thread t1, t2;

    public SearchModule() throws RemoteException {
        super();
        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();
        SearchModuleB sb = new SearchModuleB(tasks, result_pages);
        t1 = new Thread(sb);
        SearchModuleC sc = new SearchModuleC(tasks, result_pages);
        t2 = new Thread(sc);
        t1.start();
        t2.start();
    }

    private static void deleteServerSave(){
        File siFile = new File("src/databases/serverInfo.ser");
        if (siFile.exists()) {
            if (siFile.delete()) {
                System.out.println("serverInfo.ser deleted successfully.");
            } else {
                System.out.println("Failed to delete serverInfo.ser.");
            }
        } else {
            System.out.println("serverInfo.ser does not exist.");
        }
    }
    private static int readInt() {
        Scanner scanner = new Scanner(System.in);
        if (scanner.hasNextInt()) {
            int i = scanner.nextInt();
            if (i <= 0) return -1;
            return i;
        }
        scanner.nextLine();
        return -1;
    }

    // =======================================================

    public static void main(String[] args) throws RemoteException {
        new SearchModule();
        System.out.println("Search Module connections ready.");
        int exitVar = 0;
        while(exitVar != 1){
            System.out.println("To shutdown server, enter \"1\"");
            exitVar = readInt();
            if(exitVar != 1) System.out.println("Invalid option!");
        }
        deleteServerSave();
        System.exit(0);
    }
}