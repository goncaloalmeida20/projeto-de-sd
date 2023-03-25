import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseStarter {
    private static final String DB_URL = "jdbc:postgresql://localhost/";
    private static final String DB_USER = "postgres";
    private static final String DB_PASSWORD = "postgres";

    public static void main(String[] args) {
        Connection connect = null;
        Statement stm = null;
        String query;
        try {
            Class.forName("org.postgresql.Driver");
            connect = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println(0);
            stm = connect.createStatement();

            /*System.out.println("Dropping tables if they exist...");
            query = "DROP TABLE IF EXISTS inverted_index";
            stm.executeUpdate(query);

            query = "DROP TABLE IF EXISTS Links";
            stm.executeUpdate(query);

            query = "DROP TABLE IF EXISTS All_Pages";
            stm.executeUpdate(query);

            query = "DROP TABLE IF EXISTS Page";
            stm.executeUpdate(query);*/

            String sql = "CREATE DATABASE db; ";
            stm.executeUpdate(sql);

            System.out.println(1);
            // Create InvertedIndex table
            query = "CREATE TABLE inverted_index (" +
                    "Term VARCHAR(255) NOT NULL, " +
                    "UrlId INT NOT NULL, " +
                    "PRIMARY KEY (Term, UrlId)" +
                    ")";
            stm.executeUpdate(query);

            System.out.println(2);
            // Create Page table
            query = "CREATE TABLE Page (" +
                    "Id INT NOT NULL, " +
                    "Url VARCHAR(255) NOT NULL, " +
                    "Title VARCHAR(255) NOT NULL, " +
                    "Citation VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (Id)" +
                    ")";
            stm.executeUpdate(query);

            System.out.println(3);
            // Create Links table
            query = "CREATE TABLE Links (" +
                    "Id INT NOT NULL, " +
                    "PageId INT NOT NULL, " +
                    "Link VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (Id), " +
                    "FOREIGN KEY (PageId) REFERENCES Page(Id)" +
                    ")";
            stm.executeUpdate(query);

            System.out.println(4);
            // Create AllPages table
            query = "CREATE TABLE All_Pages (" +
                    "Id INT NOT NULL, " +
                    "PageId INT NOT NULL, " +
                    "PRIMARY KEY (Id), " +
                    "FOREIGN KEY (PageId) REFERENCES Page(Id)" +
                    ")";
            stm.executeUpdate(query);

            System.out.println("Tables created successfully!");
            System.out.println("Database created successfully!");
        } catch (SQLException e) {
            System.out.println("Database creation failed. Error message: " + e.getMessage());
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
                System.out.println("Error while closing connection. Error message: " + e.getMessage());
            }
        }
    }
}