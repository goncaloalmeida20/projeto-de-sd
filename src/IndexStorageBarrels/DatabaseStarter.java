package IndexStorageBarrels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseStarter {

    public static void main(String[] args) throws SQLException {
        String url = args[0];
        String name = args[1];
        String user = args[2];
        String password = args[3];
        Connection connect = null;
        Statement stm = null;

        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database...");
            connect = DriverManager.getConnection(url, user, password);

            stm = connect.createStatement();

            // Drop database if exists
            stm.executeUpdate("DROP DATABASE IF EXISTS " + name);

            stm.executeUpdate("CREATE DATABASE " + name);

            // Use the new database
            //connect = DriverManager.getConnection(url + name, user, password);
            //connect.setCatalog(name);
        } catch (SQLException e) {
            System.out.println("Database creation failed. Error message: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stm != null) {
                    stm.close();
                }
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                System.out.println("Error while closing connection with database. Error message: " + e.getMessage());
            }
        }
        //createTables(url, name, user, password);
    }
    void createTables(String url, String name, String user, String password){
        Connection connect = null;
        Statement stm = null;

        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database...");
            System.out.println(1 + " " + url + name);
            connect = DriverManager.getConnection(url + name, user, password);
            System.out.println(2);

            stm = connect.createStatement();

            // Drop tables if they already exist
            stm.executeUpdate("DROP TABLE IF EXISTS inverted_index CASCADE");
            stm.executeUpdate("DROP TABLE IF EXISTS Page CASCADE");
            stm.executeUpdate("DROP TABLE IF EXISTS Links CASCADE");
            stm.executeUpdate("DROP TABLE IF EXISTS All_Pages CASCADE");

            // Create InvertedIndex table
            String query = "CREATE TABLE inverted_index (" +
                    "Term VARCHAR(255) NOT NULL, " +
                    "UrlId INT NOT NULL, " +
                    "PRIMARY KEY (Term, UrlId)" +
                    ")";
            stm.executeUpdate(query);

            // Create Page table
            query = "CREATE TABLE Page (" +
                    "Id INT NOT NULL, " +
                    "Url VARCHAR(255) NOT NULL, " +
                    "Title VARCHAR(255) NOT NULL, " +
                    "Citation VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (Id)" +
                    ")";
            stm.executeUpdate(query);

            // Create Links table
            query = "CREATE TABLE Links (" +
                    "Id INT NOT NULL, " +
                    "PageId INT NOT NULL, " +
                    "Link VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (Id), " +
                    "FOREIGN KEY (PageId) REFERENCES Page(Id)" +
                    ")";
            stm.executeUpdate(query);

            // Create AllPages table
            query = "CREATE TABLE All_Pages (" +
                    "Id INT NOT NULL, " +
                    "PageId INT NOT NULL, " +
                    "PRIMARY KEY (Id), " +
                    "FOREIGN KEY (PageId) REFERENCES Page(Id)" +
                    ")";
            stm.executeUpdate(query);

            System.out.println("Database and tables created with success!");
        } catch (SQLException e) {
            System.out.println("Database creation failed. Error message: " + e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (stm != null) {
                    stm.close();
                }
                if (connect != null) {
                    connect.close();
                }
            } catch (SQLException e) {
                System.out.println("Error while closing connection with database. Error message: " + e.getMessage());
            }
        }
    }
}