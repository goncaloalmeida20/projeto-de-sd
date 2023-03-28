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
import java.util.Comparator;

public class BarrelModule extends UnicastRemoteObject implements BarrelModule_S_I,Runnable {
    public static SearchModuleB_S_I searchModuleB;
    public static BarrelModule b;

    public int id;

    public BarrelModule() throws RemoteException, NotBoundException {
        super();
        searchModuleB = (SearchModuleB_S_I) LocateRegistry.getRegistry(SearchModuleB.PORT).lookup(SearchModuleB.hostname);
        id = searchModuleB.connect((BarrelModule_S_I) b);
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

        //TODO: Mudar localhost
        try (Connection conn = DriverManager.getConnection("localhost");
             PreparedStatement stm = conn.prepareStatement(
                     "SELECT p.*, COUNT(l.Id) as LinksCount " +
                             "FROM Page p " +
                             "JOIN inverted_index ii ON p.Id = ii.UrlId " +
                             "LEFT JOIN Links l ON p.Id = l.PageId " +
                             "WHERE ii.Term IN (" + String.join(",", Collections.nCopies(terms.length, "?")) + ") " +
                             "GROUP BY p.Id " +
                             "HAVING COUNT(DISTINCT ii.Term) = ? " +
                             "ORDER BY LinksCount DESC"
             )) {
            for (int i = 0; i < terms.length; i++) {
                stm.setString(i + 1, terms[i]);
            }
            stm.setInt(terms.length + 1, terms.length);

            ResultSet resultSet = stm.executeQuery();
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
            while (resultSet.next()) {
                if(counter == startIndex - 1) break;
                counter++;
            }

            // Add the pages to a list
            while (resultSet.next() && counter < endIndex) {
                String urlP = resultSet.getString("Url");
                String title = resultSet.getString("Title");
                String citation = resultSet.getString("Citation");
                Page page = new Page(urlP, title, citation, null, null);
                pages.add(page);
                counter++;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return pages;
    }

    /**
     Orders the specified list of Page objects by number of links (decreasing)
     @param pages ArrayList of Page objects to order
     @return ArrayList of Pages sorted by number of links
     */
    private ArrayList<Page> order_pages(ArrayList<Page> pages){
        ArrayList<Page> pages_ordered = new ArrayList<>(pages);

        pages_ordered.sort(Comparator.comparing(Page::n_links));

        return pages_ordered;
    }

    /**
     Searches for pages that contain a specific URL in their links
     and returns the list of ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page
     @param url URL to search for in the links of all the pages with their url indexed
     @param n_page Number of the group of ten pages that shoud be return having index ∈ [totalPages / 10, totalPages / 10 + 1] equal to it
     @return ArrayList ten pages that have index ∈ [totalPages / 10, totalPages / 10 + 1] = n_page and that match the search criteria (having the URL in their links)
     @throws RemoteException If there is an error in the remote connection
     */
    public ArrayList<Page> search_pages(String url, int n_page) throws RemoteException, SQLException {
        Connection connect = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        //TODO: Mudar o localhost
        try {
            connect = DriverManager.getConnection("localhost", "postgres", "postgres");

            // Execute the query to get all Pages that have a hyperlink to the given url
            statement = connect.prepareStatement(
                    "SELECT p.* FROM Page p INNER JOIN Links l ON p.Id = l.PageId WHERE l.Link = ?"
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
            while (resultSet.next()) {
                if(counter == startIndex - 1) break;
                counter++;
            }

            // Add the pages to a list
            ArrayList<Page> pages = new ArrayList<>();
            while (resultSet.next() && counter < endIndex) {
                String urlP = resultSet.getString("Url");
                String title = resultSet.getString("Title");
                String citation = resultSet.getString("Citation");
                Page page = new Page(urlP, title, citation, null, null);
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

    @Override
    public void run() {
        try{
            System.out.println("Storage Barrel Ready");

            while (true) {

            }
        } catch (Exception e) {
            System.out.println("Exception in run: " + e);
        }
    }
}
