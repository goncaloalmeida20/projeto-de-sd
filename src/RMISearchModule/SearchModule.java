package RMISearchModule;

import classes.Page;

import java.io.*;
import java.rmi.server.*;
import java.util.*;



public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public static Thread t1, t2, t3;

    public static ServerInfo sI;


    public SearchModule() throws IOException {
        super();
        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();

        File file = new File("src/databases/serverInfo.ser");
        if (!file.exists()) {
            sI = new ServerInfo();
        } else{
            ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
            try {
                sI = (ServerInfo) ois.readObject();
                System.out.println("Server info has been recovered\n");
            } catch (ClassNotFoundException e) {
                System.out.println("Server save read: " + e.getMessage());
            }
            ois.close();
        }

        SearchModuleB sb = new SearchModuleB(tasks, result_pages);
        t1 = new Thread(sb);
        SearchModuleC sc = new SearchModuleC(tasks, result_pages);
        t2 = new Thread(sc);
        AdminModule adminModule = new AdminModule();
        t3 = new Thread(adminModule);
        t1.start();
        t2.start();
        t3.start();
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

    public static void main(String[] args) throws IOException {
        new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}