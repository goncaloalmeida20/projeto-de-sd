package IndexStorageBarrels;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;


public class DatabaseStarter {

    public static void main(String[] args) {
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

            try {
                stm.executeUpdate("CREATE DATABASE " + name);
            }
            catch(SQLException e){
                System.out.println("Database already exists");
                System.out.println("Error message: " + e.getMessage());
            }
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
}