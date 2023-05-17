package RMISearchModule;

import classes.Page;

import java.io.*;
import java.rmi.server.*;
import java.util.*;


/**
 * The SearchModule class represents the core of the search module in the RMI-based search engine application.
 * It implements the SearchModule_S_I interface which defines the search functionality.
 */
public class SearchModule extends UnicastRemoteObject implements SearchModule_S_I, Serializable {

    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public final HashMap<SearchModuleC, List<HashMap<Integer, String>>> resultsTopTen;
    public static Thread t1, t2, t3;

    public static ServerInfo sI;


    /**
     * Constructs a new SearchModule object and initializes the tasks, result pages and resultsTopTen.
     * If the file containing the server backed up information exists, it loads the information from the file.
     * If the file does not exist, it creates a new ServerInfo object.
     * It also initializes the threads to communicate with the clients, the barrels and the downloaders.
     * @throws IOException if an I/O error occurs while reading the server information file.
     */
    public SearchModule() throws IOException {
        super();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                // Code to be executed when the program is shutting down
                File file = new File("serverInfo.ser");
                ObjectOutputStream oos;
                try {
                    oos = new ObjectOutputStream(new FileOutputStream(file));
                    oos.writeObject(SearchModule.sI);
                    oos.close();
                } catch (IOException e) {
                    System.out.println("Server save before shutting down: " + e.getMessage());
                }
            }
        });

        tasks = new LinkedHashMap<>();
        result_pages = new HashMap<>();
        resultsTopTen = new HashMap<>();

        File file = new File("serverInfo.ser");
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

        SearchModuleB sb = new SearchModuleB(tasks, result_pages, resultsTopTen);
        t1 = new Thread(sb);
        SearchModuleC sc = new SearchModuleC(tasks, result_pages, resultsTopTen);
        t2 = new Thread(sc);
        AdminModule adminModule = new AdminModule();
        t3 = new Thread(adminModule);
        t1.start();
        t2.start();
        t3.start();
    }

    /**
     * Deletes the server information file.
     */
    private static void deleteServerSave(){
        File siFile = new File("serverInfo.ser");
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

    /**
     * Utility method to read an integer from standard input.
     * If the input is not an integer or is less than or equal to zero, returns -1.
     * @return the integer read from standard input, or -1 if the input is invalid
     */
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

    /**
     * Entry point for the Search Module program.
     * Creates a new instance of SearchModule and starts its threads.
     * @param args command line arguments (not used)
     * @throws IOException if an I/O error occurs while initializing the program
     */
    public static void main(String[] args) throws IOException {
        new SearchModule();
        System.out.println("Search Module connections ready.");
    }
}