package IndexStorageBarrels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseTablesCreation {
    public static void main(String[] args) {
        String url = args[0];
        String user = args[1];
        String password = args[2];
        Connection connect = null;
        Statement stm = null;

        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database...");
            System.out.println(1 + " " + url + "/");
            connect = DriverManager.getConnection(url, user, password);
            System.out.println(2);

            stm = connect.createStatement();

            // Create InvertedIndex table
            String query = "CREATE TABLE IF NOT EXISTS inverted_index (" +
                    "Term VARCHAR(255) NOT NULL, " +
                    "UrlId INT NOT NULL, " +
                    "PRIMARY KEY (Term, UrlId)" +
                    ")";
            stm.executeUpdate(query);

            // Create Page table
            query = "CREATE TABLE IF NOT EXISTS Page (" +
                    "Id INT NOT NULL, " +
                    "Url VARCHAR(255) NOT NULL, " +
                    "Title VARCHAR(255) NOT NULL, " +
                    "Citation VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (Id)" +
                    ")";
            stm.executeUpdate(query);

            // Create Links table
            query = "CREATE TABLE IF NOT EXISTS Links (" +
                    "PageId INT NOT NULL, " +
                    "Link VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (PageId, Link), " +
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
