package RMISearchModule;

import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.util.*;

import URLQueue.URLQueueStarter;
import URLQueue.URLQueue_I;
import classes.Page;
import java.rmi.registry.Registry;
import java.rmi.server.*;
import java.rmi.*;

/**
 * The SearchModuleC class implements RMI functionality for the server to the client.
 */
public class SearchModuleC extends UnicastRemoteObject implements Runnable, SearchModuleC_S_I, Serializable {
    // RMI Client info
    public static int PORT0 = 7004;
    public static String hostname0 = "127.0.0.1";


    public final Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> tasks;
    public final HashMap<SearchModuleC, ArrayList<Page>> result_pages;
    public final HashMap<SearchModuleC, List<HashMap<Integer, String>>> resultsTopTen;

    /**
     The SearchModuleC constructor initializes the tasks and result_pages fields.
     @param t the tasks associated with this client
     @param p the search results associated with this client
     @throws RemoteException if a communication error occurs during the remote method invocation
     */
    public SearchModuleC(Map<HashMap<SearchModuleC, Integer>, HashMap<Object, Integer>> t, HashMap<SearchModuleC, ArrayList<Page>> p, HashMap<SearchModuleC, List<HashMap<Integer, String>>> rtt) throws RemoteException {
        super();
        tasks = t;
        result_pages = p;
        this.resultsTopTen = rtt;
    }

    /**
     * The addTask method adds a task to the tasks field.
     * @param type the type of the task
     * @param task the task to add
     * @throws RemoteException if a communication error occurs during the remote method invocation
     */
    private void addTask(int type, HashMap<Object, Integer> task) throws RemoteException {
        synchronized (tasks){
            HashMap<SearchModuleC, Integer> cliendThreadAndTaskType = new HashMap<>();
            cliendThreadAndTaskType.put(this, type);
            tasks.put(cliendThreadAndTaskType, task);
            tasks.notify();
        }
    }

    /**
     * The saveServer method saves the server instance to a file.
     */
    private void saveServer() {
        // Create the directory if it doesn't exist
        File directory = new File("src/databases");

        // Save the instance to a file
        File file = new File(directory, "serverInfo.ser");
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream(file));
            oos.writeObject(SearchModule.sI);
            oos.close();
        } catch (IOException e) {
            System.out.println("Server save: " + e.getMessage());
        }

    }

    /**
     * The findClient method searches the client list for a given username.
     * @param username the username to search for
     * @return true if the username is found, false otherwise
     */
    private boolean findClient(String username){
        synchronized (SearchModule.sI.cIList){
            for(ClientInfo cI: SearchModule.sI.cIList){
                if(cI.username.equals(username)) return true;
            }
        }
        return false;
    }

    /**
     * Registers a new client with the given username and password.
     * @param username the username of the client to be registered
     * @param password the password of the client to be registered
     * @return the ID of the newly registered client if successful, 0 if the client already exists
     * @throws RemoteException if a remote exception occurs
     */
    public synchronized int register(String username, String password) throws RemoteException {
        boolean exist = findClient(username);
        if (exist){
            return 0; // "Client already exists!"
        } else {
            SearchModule.sI.cAllCounter++;
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.cIList.add(new ClientInfo(SearchModule.sI.cAllCounter, 0, username, password));
            }
            saveServer();
            return SearchModule.sI.cAllCounter; // "Client is now registered!"
        }

    }

    /**
     * Verifies if a client with the given username and password is logged in.
     * @param username the username of the client to be verified
     * @param password the password of the client to be verified
     * @return 0 if the client is already logged in, 1 if the client is now logged in, 2 if the credentials are invalid
     */
    private int verifyLoggedClient(String username, String password){
        int login = 2; // 0 - Already logged in -- 1 - Logged in -- 2 - Invalid credentials
        synchronized (SearchModule.sI.cIList){
            for(ClientInfo cI: SearchModule.sI.cIList){
                if(cI.username.equals(username) && cI.password.equals(password)) {
                    login = cI.logged;
                    break;
                }
            }
        }
        return login;
    }

    /**
     * Logs a client in with the given username and password
     * @param username the username of the client to be logged in
     * @param password the password of the client to be logged in
     * @param id the ID of the client to be logged in
     * @return 0 if the client is already logged in, 1 if the client is now logged in, 2 if the credentials are invalid
     * @throws RemoteException if a remote exception occurs
     */
    public int login(String username, String password, int id) throws RemoteException {
        int logged = verifyLoggedClient(username, password);
        if (logged == 0){
            return 0; // "Client already logged on!"
        } else if(logged == 1) {
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.clientInfoById(id).logged = 1;
            }
            saveServer();
            return 1; // "Client is now logged on!"
        } else{
            return 2; // "Invalid credentials"
        }
    }

    /**
     * Indexes a URL.
     * @param url the URL to be indexed
     * @throws RemoteException if a remote exception occurs
     * @throws NotBoundException if the registry is not bound
     */
    public void indexUrl(String url) throws RemoteException, NotBoundException {
        URLQueue_I uqi = (URLQueue_I) LocateRegistry.getRegistry(URLQueueStarter.URLQUEUE_PORT).lookup(URLQueueStarter.URLQUEUE_NAME);
        uqi.addURL(url);
    }

    /**
     * Searches for pages containing specified terms with pagination support.
     * @param termCount the number of terms being searched for
     * @param terms an array of terms to search for
     * @param n_page Number of the group of ten pages that should be return having index ∈ [totalPages / 10, totalPages / 10 + 1] equal to it
     * @return an ArrayList of Page objects containing the search results,
     * the list of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     * @throws RemoteException if a communication-related exception occurs
     * @throws InterruptedException if the current thread is interrupted while waiting for the search result
     */
    public ArrayList<Page> search(int termCount, String[] terms, int n_page) throws RemoteException, InterruptedException {
        HashMap<Object, Integer> task = new HashMap<>();
        task.put(terms, n_page);
        addTask(1, task);
        synchronized(result_pages) {
            while (!result_pages.containsKey(this)) {
                result_pages.wait();
            }
            ArrayList<Page> res = result_pages.get(this);
            result_pages.remove(this);
            return res;
        }
    }

    /**
     * Searches for pages that have a link to a specific URL with pagination support.
     * @param url the URL to search for pages on
     * @param n_page Number of the group of ten pages that should be return having index ∈ [totalPages / 10, totalPages / 10 + 1] equal to it
     * @param id the ID of the client performing the search
     * @param logged a boolean indicating whether the client is logged in
     * @return an ArrayList of Page objects containing the search results,
     * the list of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     * @throws RemoteException if a communication-related exception occurs
     * @throws InterruptedException if the current thread is interrupted while waiting for the search result
     */
    public ArrayList<Page> searchPages(String url, int n_page, int id, boolean logged) throws RemoteException, InterruptedException {
        if (!logged){
            return null;
        } else {
            HashMap<Object, Integer> task = new HashMap<>();
            task.put(url, n_page);
            addTask(2, task);
            synchronized(result_pages) {
                while (!result_pages.containsKey(this)) {
                    result_pages.wait();
                }
                ArrayList<Page> res = result_pages.get(this);
                result_pages.remove(this);
                return res;
            }
        }
    }

    /**
     * Retrieves a map of active downloader and barrels threads
     * @return a Map with Integer keys representing thread IDs and Integer values representing the current progress of each thread
     * @throws RemoteException if a communication-related exception occurs
     */
    public Map<Integer, Integer> admin() throws RemoteException {
        return AdminModule.getActiveDownloaderAndBarrels();
    }

    /**
     Logs a client out of the system.
     @param id the ID of the client to log out
     @return an integer indicating the success or failure of the logout attempt (0 for failure, 1 for success)
     @throws RemoteException if a communication-related exception occurs
     */
    public int logout(int id) throws RemoteException {
        int logged;
        synchronized (SearchModule.sI.cIList){
            logged = SearchModule.sI.clientInfoById(id).logged;
        }
        if (logged == 0){
            return 0; // "Client is not logged on, so it cannot log out!"
        } else {
            synchronized (SearchModule.sI.cIList){
                SearchModule.sI.clientInfoById(id).logged = 0;
                saveServer();
            }
            return 1; // "Client is now logged off!"
        }
    }

    /**
     * Retrieves the top ten searches from the database.
     * This method uses a synchronized block to add the task to the task list and wait for the result.
     * @return a List of HashMaps containing the top ten searches, where the key is the type and the value is the search string.
     * @throws RemoteException if a communication-related exception occurs
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
     public List<HashMap<Integer, String>> getTopTenSeaches() throws RemoteException, InterruptedException {
        HashMap<Object, Integer> task = new HashMap<>();
        task.put(null, null);
        addTask(3, task);
        synchronized(resultsTopTen) {
            while (!resultsTopTen.containsKey(this)) {
                resultsTopTen.wait();
            }
            List<HashMap<Integer, String>> res = resultsTopTen.get(this);
            resultsTopTen.remove(this);
            return res;
        }
    }

    // =======================================================

    /**
     * This method starts a server for the Search Module, creates a registry, and binds the SearchModule object to it.
     * Once the binding is complete, the method prints a message indicating that the connection is ready.
     * @throws RemoteException if there is an error with the remote communication
     */
    public void run() {
        try {
            Registry rC = LocateRegistry.createRegistry(PORT0);
            rC.rebind(hostname0, this);

            System.out.println("Search Module - Client connection ready.");
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}