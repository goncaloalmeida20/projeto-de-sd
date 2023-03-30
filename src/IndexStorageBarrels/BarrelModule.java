package IndexStorageBarrels;

import RMISearchModule.SearchModuleB;
import RMISearchModule.SearchModuleB_S_I;
import classes.Page;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;

public class BarrelModule extends UnicastRemoteObject implements BarrelModule_S_I,Runnable {
    public static SearchModuleB_S_I searchModuleB;

    public int id;

    public BarrelModule(int id) throws RemoteException, NotBoundException {
        super();
        /*File file = new File("src/databases/serverInfo.ser");
        boolean serverActive = false;
        if (file.exists()) {
            long finish = System.currentTimeMillis() + 15000; // End time
            while (System.currentTimeMillis() < finish && !serverActive) {
                try {
                    searchModuleB = (SearchModuleB_S_I) LocateRegistry.getRegistry(SearchModuleB.PORT).lookup(SearchModuleB.hostname);
                    serverActive = true;
                } catch (ConnectException | NotBoundException ex) {
                    System.out.println("The server continues shutdown!");
                }
                if (serverActive) System.out.println("Connection to server was recovered!");
            }
            if (!serverActive) System.out.println("Connection closed.");
        } else{
            searchModuleB = (SearchModuleB_S_I) LocateRegistry.getRegistry(SearchModuleB.PORT).lookup(SearchModuleB.hostname);
            File barrelId = new File("src/databases/barrelId.ser");
            if(barrelId.exists()){
                try {
                    FileInputStream barrelIn = new FileInputStream(barrelId);
                    ObjectInputStream bIn = new ObjectInputStream(barrelIn);
                    id = (Integer) bIn.readObject();
                    System.out.println("Barrel id has been recovered\n");
                } catch (IOException | ClassNotFoundException e) {
                    e.printStackTrace();
                }
            } else{
                id = searchModuleB.connect((BarrelModule_S_I) this);
                try {
                    FileOutputStream fileOut = new FileOutputStream("src/databases/barrelId.ser");
                    ObjectOutputStream Objout = new ObjectOutputStream(fileOut);
                    Objout.writeObject(id);

                    Objout.close();
                    fileOut.close();

                    System.out.println("BarrelId object saved in barrelId.ser");
                } catch (IOException e) {
                    System.out.println("Barrel id save: " + e.getMessage());
                }
            }
        }*/
        this.id = id;
        searchModuleB = (SearchModuleB_S_I) LocateRegistry.getRegistry(SearchModuleB.PORT).lookup(SearchModuleB.hostname);
        searchModuleB.connect((BarrelModule_S_I) this, id);
    }

    /**
     Searches for pages that contain all the specified search terms, order them
     and returns the list of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     @param terms Array of terms to match in the pages
     @param n_page Number of the group of ten pages that shoud be return having index ∈ [totalPages / 10, totalPages / 10 + 1] equal to it
     @return ArrayList of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     @throws RemoteException If there is an error with the remote connection
     */
    public ArrayList<Page> search(String[] terms, int n_page) throws RemoteException {
        ArrayList<Page> pages = new ArrayList<>();

        try (Connection conn = DriverManager.getConnection(Barrel.bdb.urldb, Barrel.bdb.user, Barrel.bdb.password);
             PreparedStatement stm = conn.prepareStatement(
                     "SELECT p.*, COUNT(l.pageid) AS linkcount " +
                             "FROM page p " +
                             "JOIN invertedindex ii ON p.id = ii.pageid " +
                             "LEFT JOIN links l ON p.id = l.pageid " +
                             "WHERE ii.term IN (" + String.join(",", Collections.nCopies(terms.length, "?")) + ") " +
                             "GROUP BY p.id " +
                             "HAVING COUNT(DISTINCT ii.term) = ? " +
                             "ORDER BY linkcount DESC;",
                     ResultSet.TYPE_SCROLL_INSENSITIVE,
                     ResultSet.CONCUR_READ_ONLY
             )) {

            for (int i = 0; i < terms.length; i++) {
                stm.setString(i + 1, terms[i]);
            }
            stm.setInt(terms.length + 1, terms.length);
            //System.out.println(stm.toString());

            ResultSet resultSet = stm.executeQuery();

            // Get total number of pages that have a hyperlink to the given url
            int lenPages = 0;
            while (resultSet.next()) {
                lenPages++;
            }

            // Calculate the index range
            int startIndex = (lenPages / 10) * n_page;
            int endIndex = startIndex + 10;

            // Set the result iterator in the startIndex
            int counter = 0;
            resultSet.beforeFirst();
            while (resultSet.next()) {
                if(counter == startIndex - 1) break;
                counter++;
            }

            // Add the pages to a list
            resultSet.beforeFirst();
            while (resultSet.next() && counter < endIndex) {
                Page page = new Page();
                page.url = resultSet.getString("url");
                page.title = resultSet.getString("title");
                page.citation = resultSet.getString("citation");
                pages.add(page);
                counter++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return pages;
    }

    /**
     Searches for pages that contain a specific URL in their links
     and returns the list of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     @param url URL to search for in the links of all the pages with their url indexed
     @param n_page Number of the group of ten pages that shoud be return having index ∈ [totalPages / 10, totalPages / 10 + 1] equal to it
     @return ArrayList ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page and that match the search criteria (having the URL in their links)
     @throws RemoteException If there is an error in the remote connection
     */
    public ArrayList<Page> search_pages(String url, int n_page) throws RemoteException {
        Connection connect = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            connect = DriverManager.getConnection(Barrel.bdb.urldb, Barrel.bdb.user, Barrel.bdb.password);

            // Execute the query to get all Pages that have a hyperlink to the given url
            statement = connect.prepareStatement(
                    "SELECT p.* FROM Page p INNER JOIN Links l ON p.Id = l.PageId WHERE l.Link = ?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY
            );
            statement.setString(1, url);
            resultSet = statement.executeQuery();
            ResultSet copyResultSet = resultSet;

            // Get total number of pages that have a hyperlink to the given url
            int lenPages = 0;
            while (copyResultSet.next()) {
                lenPages++;
            }

            // Calculate the index range
            int startIndex = (lenPages / 10) * n_page;
            int endIndex = startIndex + 10;

            // Set the result iterator in the startIndex
            int counter = 0;
            resultSet.beforeFirst();
            while (resultSet.next()) {
                if(counter == startIndex - 1) break;
                counter++;
            }

            // Add the pages to a list
            ArrayList<Page> pages = new ArrayList<>();
            resultSet.beforeFirst();
            while (resultSet.next() && counter < endIndex) {
                Page page = new Page();
                page.url = resultSet.getString("url");
                page.title = resultSet.getString("title");
                page.citation = resultSet.getString("citation");
                pages.add(page);
                counter++;
            }
            return pages;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            // Close the result set, statement, and connection
            try {
                if (resultSet != null) {
                    resultSet.close();
                }
                if (statement != null) {
                    statement.close();
                }
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     Returns the id of this Barrel
     @return the id of the Barrel
     @throws RemoteException If there is an error in the remote connection
     */
    public int getId() throws RemoteException {
        return id;
    }

    public void ping() throws RemoteException {

    }

    @Override
    public void run() {
        try{
            System.out.println("Barrel Module Ready");

            while (true) {

            }
        } catch (Exception e) {
            System.out.println("Exception in run: " + e);
        }
    }
}
