package RMIClient;

import RMISearchModule.SearchModuleC;
import RMISearchModule.SearchModuleC_S_I;
import classes.Page;

import java.rmi.ConnectException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.Registry;
import java.util.*;
import java.io.*;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.*;

/**
 * The ClientInterface class represents the client interface for the RMI search system.
 * It allows clients to register and login, as well as search for pages using the SearchModuleC_S_I interface.
 */
public class ClientInterface extends UnicastRemoteObject implements ClientInterface_C_I {
    private static final Scanner scanner = new Scanner(System.in);

    private static SearchModuleC_S_I searchM;
    private static ClientAskedInfo cAI;

    private static int id, op = 0;
    private static boolean logged = false;
    private static boolean serverActive;

    public ClientInterface() throws RemoteException {
        super();
    }

    /**
     * Reads a string input from the user, handling empty strings.
     * @return the string input read from the user
     */
    private static String readString() {
        String str;
        do {
            str = scanner.nextLine();
        } while (str.isEmpty());
        return str;
    }

    /**
     * Reads an integer input from the user, handling invalid input.
     * @return the integer input read from the user, or -1 if the input is invalid
     */
    private static int readInt() {
        if (scanner.hasNextInt()) {
            int i = scanner.nextInt();
            if (i <= 0) return -1;
            return i;
        }
        scanner.nextLine();
        return -1;
    }

    /**
     * Registers the client with the server by asking the user for a username and password.
     * @throws IOException if there is an error reading input from the user
     * @throws ServerNotActiveException if the client is not active on the server
     */
    private static void register() throws IOException, ServerNotActiveException {
        String username, password;
        System.out.print("Username: ");
        username = readString();
        if (username == null) {
            System.out.println("Invalid username!");
        } else {
            System.out.print("Password: ");
            password = readString();
            if (password == null) {
                System.out.println("Invalid password!");
            } else {
                cAI.username = username;
                cAI.password = password;
                registerRecover();
            }
        }
    }

    /**
     * Registers the client with the server and displays a message indicating the result of the registration.
     * @throws RemoteException if there is a communication-related exception
     */
    private static void registerRecover() throws RemoteException {
        int reg = searchM.register(cAI.username, cAI.password);
        serverActive = true;
        String msg;
        if (reg == 0) msg = "Client already exists!";
        else {
            msg = "Client is now registered!";
            id = reg;
        }
        System.out.println("\nServer message: " + msg + '\n');
    }

    /**
     * Logins the client with the server by asking the user for a username and password.
     * @throws IOException If there is an I/O error while reading the input.
     * @throws ServerNotActiveException If the server is not active.
     */
    private static void login() throws IOException, ServerNotActiveException {
        String username, password;
        System.out.print("Username: ");
        username = readString();
        if (username == null) {
            System.out.println("Invalid username!");
        } else {
            System.out.print("Password: ");
            password = readString();
            if (password == null) {
                System.out.println("Invalid password!");
            } else {
                cAI.username = username;
                cAI.password = password;
                loginRecover();
            }
        }
    }

    /**
     * Logins the client with the server and displays a message indicating the result of the registration.
     * @throws RemoteException if there is a communication-related exception
     */
    public static void loginRecover() throws ServerNotActiveException, RemoteException {
        int login = searchM.login(cAI.username, cAI.password, id);
        serverActive = true;
        String msg;
        if (login == 1) msg = "Client already logged on!";
        else if (login == 0) {
            msg = "Client is now logged on!";
            logged = true;
        } else msg = "Invalid credentials!";
        System.out.println("\nServer message: " + msg + '\n');
    }

    /**
     * Indexes a URL provided by the user after setting the URL to the current active instance's URL.
     * @throws IOException If there is an I/O error while reading user input.
     * @throws NotBoundException If the remote object is not bound to the registry.
     */
    private static void indexUrl() throws IOException, NotBoundException {
        String url;
        System.out.print("\nUrl to index: ");
        url = readString();
        if (url == null) {
            System.out.println("Invalid url!");
        } else {
            cAI.url = url;
            indexUrlRecover();
        }
    }

    /**
     Calls the remote method "search" to index the URL of the active instance's URL
     @throws NotBoundException If the remote object is not bound to the registry.
     @throws RemoteException If a communication error occurs during the remote method invocation.
     */
    private static void indexUrlRecover() throws NotBoundException, RemoteException {
        searchM.indexUrl(cAI.url.toLowerCase());
        serverActive = true;
        System.out.println("Url is going indexed!");
    }

    /**
     * Searches for pages that contain the terms given by the user
     * @throws IOException If there is an I/O error while reading user input.
     * @throws NotBoundException If the remote object is not bound to the registry.
     * @throws InterruptedException If the current thread is interrupted while waiting for the user input.
     */
    private static void search() throws IOException, NotBoundException, InterruptedException {
        int termCount, n_page = 0;
        String[] terms;
        System.out.print("\nNumber of terms to search: ");
        termCount = readInt();
        if (termCount <= 0) {
            System.out.println("Invalid number!");
        } else {
            cAI.termCount = termCount;
            terms = new String[termCount];
            for (int i = 0; i < termCount; i++) {
                System.out.print("Term " + i + ": ");
                terms[i] = readString();
                if (terms[i] == null) {
                    System.out.println("Invalid term!");
                    n_page = -1;
                    break;
                } else terms[i] = terms[i].toLowerCase();
            }
            if (n_page == 0) {
                cAI.terms = terms;
                System.out.print("Number of the page to show: ");
                n_page = readInt();
                if (n_page < 0) {
                    System.out.println("Invalid number!");
                } else {
                    cAI.n_page = n_page;
                    searchRecover();
                }
            }
        }
    }

    /**
     * Search for pages that contain the specified search terms and page number.
     * @throws NotBoundException If the remote object is not bound to the registry.
     * @throws RemoteException If a communication error occurs during the remote method invocation.
     * @throws InterruptedException If the current thread is interrupted while waiting for the user input.
     */
    private static void searchRecover() throws NotBoundException, RemoteException, InterruptedException {
        ArrayList<Page> pages = searchM.search(cAI.termCount, cAI.terms, cAI.n_page);
        serverActive = true;
        if (pages == null) System.out.println("There are no pages that corresponds to the request " +
                "or there weren't barrels to respond to this request in an interval of 10 seconds\n");
        else {
            if (pages.size() == 0) {
                System.out.println("There are no pages that corresponds to the request\n");
            } else {
                System.out.print("Pages that contain the terms ");
                for (int i = 0; i < cAI.termCount; i++) {
                    System.out.print(cAI.terms[i] + ' ');
                }
                System.out.println("- Page " + cAI.n_page);
                for (Page page : pages) {
                    System.out.println("Title of the page: " + page.title);
                    System.out.println("Complete Url: " + page.url);
                    System.out.println("Short citation: " + page.citation + '\n');
                }
            }
        }
    }

    /**
     * Search pages that have a link to the given URL
     * @throws IOException if there is an I/O error while reading input
     * @throws NotBoundException if the remote object is not bound in the registry
     * @throws ServerNotActiveException if a remote method is called outside a server request
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    private static void searchPages() throws IOException, NotBoundException, ServerNotActiveException, InterruptedException {
        String url;
        int n_page;
        System.out.print("\nUrl: ");
        url = readString();
        if (url == null) {
            System.out.println("Invalid url!");
        } else {
            cAI.url = url;
            System.out.print("Number of the page to show: ");
            n_page = readInt();
            if (n_page < 0) {
                System.out.println("Invalid number!");
            } else {
                cAI.n_page = n_page;
                searchPagesRecover();
            }
        }
    }

    /**
     * Search pages that have a link to the given URL, in case of failure recovers the search.
     * @throws ServerNotActiveException if a remote method is called outside a server request
     * @throws NotBoundException if the remote object is not bound in the registry
     * @throws RemoteException if there is a communication-related error while using the remote object
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    private static void searchPagesRecover() throws ServerNotActiveException, NotBoundException, RemoteException, InterruptedException {
        ArrayList<Page> pages = searchM.searchPages(cAI.url.toLowerCase(), cAI.n_page, id, logged);
        serverActive = true;
        if (pages == null) {
            System.out.println("Client needs to be logged on to perform this operation or there are no pages that corresponds to the request " +
                    "or there weren't barrels to respond to this request in an interval of 10 seconds\n");
        } else {
            if (pages.size() == 0) {
                System.out.println("There are no pages that corresponds to the request\n");
            } else {
                System.out.print("Pages that have a link to " + cAI.url);
                System.out.println(" - Page " + cAI.n_page);
                for (int i = 0; i < pages.size(); i++) {
                    System.out.println("Url " + (i + 1) + ": " + pages.get(i).url);
                }
            }
        }
    }

    /**
     * Access the administration panel.
     * @throws IOException if there is an I/O error while reading input
     */
    private static void admin() throws IOException, InterruptedException {
        Map<Integer, Integer> info = searchM.admin();
        List<HashMap<Integer, String>> topTenSearches = searchM.getTopTenSeaches();
        int activeBarrels, activeDownloaders;
        if (!info.isEmpty()) {
            Map.Entry<Integer, Integer> entry = info.entrySet().iterator().next();
            activeBarrels = entry.getKey();
            activeDownloaders = entry.getValue();
            System.out.println("\n-----------------------Administration panel-----------------------\n");
            if(activeBarrels == 0) System.out.println("No barrels are active");
            else {
                System.out.println("\t\tNumber of active barrels: " + activeBarrels);
            }
            if(activeDownloaders == 0) System.out.println("No downloaders are active");
            else {
                System.out.println("\t\tNumber of active downloaders: " + activeDownloaders);
            }
        } else System.out.println("Couldn't get any information about the active downloaders and barrels");
        System.out.println("\n          ---------------Top Ten Searches---------------          ");
        if(topTenSearches == null){
            System.out.println("There weren't barrels to respond to this request in an interval of 10 seconds\n");
        }else {
            if (topTenSearches.isEmpty()) {
                System.out.println("                   No Searches have been done yet                   ");
            } else {
                //System.out.println("topTenSearches size: " + topTenSearches.size());
                final int[] counter = {1};

                for (HashMap<Integer, String> tTS : topTenSearches) {
                    tTS.forEach((key, value) -> {
                        System.out.print("\t\t" + counter[0] + ". ");
                        if (key == 1) {
                            System.out.println("Term: " + value);
                        } else if (key == 2) {
                            System.out.println("Url: " + value);
                        }
                        counter[0]++;
                    });
                }
            }
        }
        System.out.println("\n------------------------------------------------------------------\n");
    }

    /**
     * Logs out the user from the remote search module.
     * @throws IOException if there's an I/O error while communicating with the server
     * @throws ServerNotActiveException if the server is not active
     */
    private static void logout() throws IOException, ServerNotActiveException {
        int logout = searchM.logout(id);
        serverActive = true;
        String msg;
        if (logout == 1) msg = "Client is not logged on, so it cannot logout!"; // Nunca acontece
        else msg = "Client is now logged off!";
        logged = false;
        System.out.println("Server message: " + msg + "\n");
    }

    /**
     * Exits the client application.
     * @throws ServerNotActiveException if the server is not active
     * @throws RemoteException if there's a remote exception while communicating with the server
     */
    private static void exit() throws ServerNotActiveException, RemoteException {
        searchM.logout(id);
        serverActive = true;
        System.out.println("Close client menu...");
    }

    /**
     * Connects the client to the remote search module (Server).
     * @throws RemoteException if there's a remote exception while communicating with the server
     * @throws NotBoundException if the remote search module is not bound to the registry
     */
    private static void connectToServer() throws RemoteException, NotBoundException {
        Registry r = LocateRegistry.getRegistry(SearchModuleC.PORT0);
        searchM = (SearchModuleC_S_I) r.lookup(SearchModuleC.hostname0);
        serverActive = true;
        cAI = new ClientAskedInfo();
    }

    /**
     * Displays the client menu and handles user input.
     */
    private static void showMenu() {
        try {
            int count;
            do {
                count = 1;
                System.out.println("Client Menu:");
                if (!logged) System.out.println(count++ + " - Registar");
                if (!logged) System.out.println(count++ + " - Fazer login");
                System.out.println(count++ + " - Indexar novo URL");
                System.out.println(count++ + " - Pesquisar páginas que contenham um conjunto de termos");
                if (logged)
                    System.out.println(count++ + " - Consultar lista de páginas com ligação para uma página específica");
                System.out.println(count++ + " - Consultar página de administração");
                if (logged) System.out.println(count++ + " - Fazer logout");
                System.out.println(count + " - Sair do programa");
                System.out.print("Option: ");
                op = readInt();
                if (!logged) {
                    switch (op) {
                        case 1 -> register();
                        case 2 -> login();
                        case 3 -> indexUrl();
                        case 4 -> search();
                        case 5 -> admin();
                        case 6 -> exit();
                        default -> System.out.println("Invalid option!");
                    }
                } else {
                    switch (op) {
                        case 1 -> indexUrl();
                        case 2 -> search();
                        case 3 -> searchPages();
                        case 4 -> admin();
                        case 5 -> logout();
                        case 6 -> exit();
                        default -> System.out.println("Invalid option!");
                    }
                }
            } while (op != 6);
        } catch (ServerNotActiveException | NotBoundException | IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * The main class that runs the client-side program.
     * It connects to the server, displays a menu with different options, and handles the user's input.
     * If the server goes down during an action, the program will retry the last action for 10 seconds before shutting down.
     * @param args the command-line arguments (not used)
     * @throws InterruptedException if the thread is interrupted while waiting for a response from the server
     * @throws ServerNotActiveException if the server is not currently active
     * @throws IOException if there is an I/O error while communicating with the server
     * @throws NotBoundException if the registry does not contain an entry for the specified name in the specified port
     */
    public static void main(String[] args) throws InterruptedException, ServerNotActiveException, IOException, NotBoundException {
        while (op != 6) {
            try {
                connectToServer();
                showMenu();
                break;
            } catch (Exception e) {
                //StackTraceElement[] elements = e.getStackTrace();
                //for (int iterator=elements.length-1; iterator>0; iterator--) System.out.println(elements[iterator-1].getMethodName());
                if (cAI != null) {
                    System.out.println("Server went down or no Barrels Available! " +
                            "Retrying the last action for 10sec, after that client will be shutdown.");
                    serverActive = false;
                    long finish = System.currentTimeMillis() + 10000; // End time
                    while (System.currentTimeMillis() < finish && !serverActive) {
                        try {
                            Registry r = LocateRegistry.getRegistry(SearchModuleC.PORT0);
                            searchM = (SearchModuleC_S_I) r.lookup(SearchModuleC.hostname0);
                            if (!logged) {
                                switch (op) {
                                    case 1 -> registerRecover();
                                    case 2 -> loginRecover();
                                    case 3 -> indexUrlRecover();
                                    case 4 -> searchRecover();
                                    case 5 -> searchPagesRecover();
                                    case 6 -> admin();
                                    case 7 -> exit();
                                    default -> System.out.println("Invalid option!");
                                }
                            } else {
                                switch (op) {
                                    case 1 -> indexUrlRecover();
                                    case 2 -> searchRecover();
                                    case 3 -> searchPagesRecover();
                                    case 4 -> admin();
                                    case 5 -> logout();
                                    case 6 -> exit();
                                    default -> System.out.println("Invalid option!");
                                }
                            }
                        } catch (ConnectException ex) {
                            System.out.println("The server continues shutdown!");
                        }
                        if (serverActive) System.out.println("Connection to server was recovered!");
                    }
                    if (!serverActive) {
                        System.out.println("Connection closed.");
                        break;
                    }
                } else {
                    System.out.println("Server went down but there wasn't performed any action so the connection will be shutdown.");
                    break;
                }
            }
        }
    }
}