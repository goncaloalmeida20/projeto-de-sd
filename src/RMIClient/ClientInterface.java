package RMIClient;

import RMISearchModule.SearchModuleC;
import RMISearchModule.SearchModuleC_S_I;
import classes.Page;

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
    private static int id;

    public String username, password;

    public ClientInterface(String username, String password) throws RemoteException {
        super();
        this.username = username;
        this.password = password;
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

    public static void main(String[] args) {
        try {
            Registry r = LocateRegistry.getRegistry(SearchModuleC.PORT0);
            searchM = (SearchModuleC_S_I) r.lookup(SearchModuleC.hostname0);
            id = searchM.connectSM();
            int op;
            do {
                System.out.println("Client Menu:");
                System.out.println("1 - Fazer login");
                System.out.println("2 - Indexar novo URL");
                System.out.println("3 - Pesquisar páginas que contenham um conjunto de termos");
                System.out.println("4 - Consultar lista de páginas com ligação para uma página específica");
                System.out.println("5 - Consultar página de administração");
                System.out.println("6 - Fazer logout");
                System.out.println("7 - Sair do programa");
                System.out.print("Option: ");
                op = readInt();
                switch (op) {
                    case 1 -> login();
                    case 2 -> indexUrl();
                    case 3 -> search();
                    case 4 -> searchPages();
                    case 5 -> admin();
                    case 6 -> logout();
                    case 7 -> searchM.logout(id);
                    default -> System.out.println("Invalid option!");
                }
            } while (op != 7);
        } catch (IOException e) {
            System.out.println("IO_1: " + e.getMessage());
        } catch (NotBoundException | ServerNotActiveException | InterruptedException e) {
            e.printStackTrace();
        }
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
                String msg = searchM.login(username, password, id);
                System.out.println("Server message: " + msg);
                System.out.println();
            }
        }
    }

    private static void indexUrl() throws IOException, NotBoundException {
        String url;
        System.out.print("\nUrl to index: ");
        url = readString();
        if (url == null) {
            System.out.println("Invalid url!");
        } else {
            searchM.indexUrl(url);
        }
    }

    private static void search() throws IOException, NotBoundException, InterruptedException {
        int termCount, n_page = 0;
        String[] terms;
        System.out.print("\nNumber of terms to search: ");
        termCount = readInt();
        if (termCount <= 0) {
            System.out.println("Invalid number!");
        } else {
            terms = new String[termCount];
            for (int i = 0; i < termCount; i++) {
                System.out.print("Term " + i + ": ");
                terms[i] = readString();
                if (terms[i] == null) {
                    System.out.println("Invalid term!");
                    n_page = -1;
                    break;
                }
            }
            if (n_page == 0) {
                System.out.print("Number of the page to show: ");
                n_page = readInt();
                if (n_page < 0) {
                    System.out.println("Invalid number!");
                } else {
                    System.out.print("Pages that contain the terms ");
                    for (int i = 0; i < termCount; i++) {
                        System.out.print(terms[i] + ' ');
                    }
                    System.out.println("- Page " + n_page);

                    ArrayList<Page> pages = searchM.search(termCount, terms, n_page);
                    if (pages.size() == 0) System.out.println("There are no pages that corresponds to the request\n");
                    else {
                        for (Page page : pages) {
                            System.out.println("Title of the page" + page.title);
                            System.out.println("Complete Url: " + page.url);
                            System.out.println("Short citation: " + page.citation + '\n');
                        }
                        System.out.println();
                    }
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
            System.out.print("Number of the page to show: ");
            n_page = readInt();
            if (n_page < 0) {
                System.out.println("Invalid number!");
            } else {
                ArrayList<Page> pages = searchM.searchPages(url, n_page, id);
                if (pages == null) {
                    System.out.println("Client needs to be logged on to perform this operation!");
                } else {
                    if (pages.size() == 0) System.out.println("There are no pages that corresponds to the request\n");
                    else {
                        System.out.print("Pages that have a link to " + url);
                        System.out.println(" - Page " + n_page);
                        for (int i = 0; i < pages.size(); i++) {
                            System.out.println("Url " + (i + 1) + ": " + pages.get(i).url);
                        }
                        System.out.println();
                    }
                }
            }
        }
    }

    private static void admin() throws IOException {
        searchM.admin();
    }

    private static void logout() throws IOException, ServerNotActiveException {
        String msg = searchM.logout(id);
        System.out.println("Server message: " + msg);
    }
}