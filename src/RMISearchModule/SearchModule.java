package RMISearchModule;

import classes.Page;

import java.io.*;
import java.rmi.server.*;
import java.rmi.*;
import java.util.*;



public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public static Thread t1, t2;

    public static ServerInfo sI;

    public SearchModule() throws RemoteException {
        super();
        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();

        File file = new File("src/databases/serverInfo.ser");
        if (file.exists()) {
            try {
                FileInputStream fileIn = new FileInputStream(file);
                ObjectInputStream in = new ObjectInputStream(fileIn);
                sI = (ServerInfo) in.readObject();
                System.out.println("Server info has been recovered\n");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        } else {
            sI = new ServerInfo();
        }

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
                System.out.println("Failed to delete serverInfo.ser. Delete it manually!");
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