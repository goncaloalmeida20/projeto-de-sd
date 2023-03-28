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

public class ClientInterface extends UnicastRemoteObject implements ClientInterface_C_I {
    private static final Scanner scanner = new Scanner(System.in);

    private static SearchModuleC_S_I searchM;
    private static ClientAskedInfo cAI;

    private static int id, op = 0;
    private static boolean logged;
    private static boolean serverActive;

    public ClientInterface() throws RemoteException {
        super();
        this.logged = false;
    }

    private static String readString() {
        String str;
        do {
            str = scanner.nextLine();
        } while (str.isEmpty());
        return str;
    }

    private static int readInt() {
        if (scanner.hasNextInt()) {
            int i = scanner.nextInt();
            if (i <= 0) return -1;
            return i;
        }
        scanner.nextLine();
        return -1;
    }

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

    private static void registerRecover() throws RemoteException {
        int reg = searchM.register(cAI.username, cAI.password, (SearchModuleC_S_I) searchM);
        serverActive = true;
        String msg;
        if (reg == 0) msg = "Client already exists!";
        else {
            msg = "Client is now registered!";
            id = reg;
        }
        System.out.println("\nServer message: " + msg + '\n');
    }

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

    public static void loginRecover() throws ServerNotActiveException, RemoteException {
        System.out.println(cAI.username + " " + cAI.password + " " + id);
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

    private static void indexUrlRecover() throws NotBoundException, RemoteException {
        searchM.indexUrl(cAI.url.toLowerCase());
        serverActive = true;
    }

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

    private static void searchRecover() throws NotBoundException, RemoteException, InterruptedException {
        ArrayList<Page> pages = searchM.search(cAI.termCount, cAI.terms, cAI.n_page);
        serverActive = true;
        if (pages == null) System.out.println("There are no pages that corresponds to the request\n");
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
                    System.out.println("Title of the page" + page.title);
                    System.out.println("Complete Url: " + page.url);
                    System.out.println("Short citation: " + page.citation + '\n');
                }
            }
        }
    }

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

    private static void searchPagesRecover() throws ServerNotActiveException, NotBoundException, RemoteException, InterruptedException {
        ArrayList<Page> pages = searchM.searchPages(cAI.url.toLowerCase(), cAI.n_page, id);
        serverActive = true;
        if (pages == null) {
            System.out.println("Client needs to be logged on to perform this operation or there are no pages that corresponds to the request!");
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

    private static void admin() throws IOException {
        searchM.admin();
    }

    private static void logout() throws IOException, ServerNotActiveException {
        int logout = searchM.logout(id);
        serverActive = true;
        String msg;
        if (logout == 1) msg = "Client is not logged on, so it cannot logout!"; // Nunca acontece
        else msg = "Client is now logged off!";
        logged = false;
        System.out.println("Server message: " + msg + "\n");
    }

    private static void exit() throws ServerNotActiveException, RemoteException {
        searchM.logout(id);
        serverActive = true;
        System.out.println("Close client menu...");
    }

    private static void connectToServer() throws RemoteException, NotBoundException {
        Registry r = LocateRegistry.getRegistry(SearchModuleC.PORT0);
        searchM = (SearchModuleC_S_I) r.lookup(SearchModuleC.hostname0);
        serverActive = true;
        cAI = new ClientAskedInfo();
    }

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

    public static void main(String[] args) throws InterruptedException, ServerNotActiveException, IOException, NotBoundException {
        while (op != 6) {
            try {
                connectToServer();
                showMenu();
                break;
            } catch (Exception e) {
                if (cAI != null) {
                    System.out.println("Server went down! Retrying the last action for 10sec, after that client will be shutdown.");
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