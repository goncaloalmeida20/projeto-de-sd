package IndexStorageBarrels;

import classes.Page;

import java.sql.*;

public class BarrelDatabase {
    public int barrelId;
    public String url, dbName, user, password, urldb;

    public BarrelDatabase(String url, int barrelId, String user, String password){
        this.url = url;
        this.dbName = "barrel" + barrelId + "db";
        this.urldb = url + this.dbName;
        this.user = user;
        this.password = password;
        startDatabase();
        loadTables();
        Page p = new Page();
        p.url = "testeurl";
        p.title = "testetitle";
        p.citation = "testecitation";
        p.words.add("teste1");
        p.words.add("teste2");
        p.words.add("teste3");
        p.links.add("www.ph.com");
        p.links.add("www.xv.com");
        insertPage(p);
    }

    private void startDatabase(){
        Connection connect = null;
        Statement stm = null;

        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database...");
            connect = DriverManager.getConnection(url, user, password);

            stm = connect.createStatement();

            try {
                stm.executeUpdate("CREATE DATABASE " + dbName);
            }
            catch(SQLException e){
                System.out.println("Database " + dbName + " exists");
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
    }

    private void loadTables(){
        Connection connect = null;
        Statement stm = null;
        String query;
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb, user, password);

            stm = connect.createStatement();

            // Create Page table
            query = "CREATE TABLE IF NOT EXISTS page (" +
                    "id SERIAL, " +
                    "url VARCHAR(255) NOT NULL, " +
                    "title VARCHAR(255) NOT NULL, " +
                    "citation VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (id) " +
                    ")";
            stm.executeUpdate(query);

            // Create InvertedIndex table
            query = "CREATE TABLE IF NOT EXISTS invertedindex (" +
                    "term VARCHAR(255) NOT NULL, " +
                    "pageid INT REFERENCES page(id) NOT NULL, " +
                    "PRIMARY KEY (term, pageid)" +
                    ")";
            stm.executeUpdate(query);

            // Create Links table
            query = "CREATE TABLE IF NOT EXISTS links (" +
                    "pageid INT REFERENCES page(id) NOT NULL, " +
                    "link VARCHAR(255) NOT NULL, " +
                    "PRIMARY KEY (pageid, link)" +
                    ")";
            stm.executeUpdate(query);
            
            System.out.println("Tables loaded with success!");
        } catch (SQLException e) {
            System.out.println("Table loading failed. Error message: " + e);
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

    public void insertPage(Page p){
        Connection connect = null;
        PreparedStatement stm = null;
        String query;
        StringBuilder sb;
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb , user, password);

            query = "INSERT INTO page(url, title, citation) " +
                    "VALUES (?, ?, ?);";

            stm = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, p.url);
            stm.setString(2, p.title);
            stm.setString(3, p.citation);
            stm.executeUpdate();
            ResultSet rs = stm.getGeneratedKeys();
            long insertId = -1;
            if(rs.next())
                insertId = rs.getLong(1);
            if(insertId == -1) throw new RuntimeException("Error on returned generated keys");

            sb = new StringBuilder();
            sb.append("INSERT INTO invertedindex(term, pageid) " +
                    "VALUES ");
            for(int i = 0; i < p.words.size(); i++){
                if(i != 0) sb.append(", ");
                sb.append("(?, ").append(insertId).append(")");
            }
            sb.append(";");
            query = sb.toString();
            stm = connect.prepareStatement(query);
            for(int i = 0; i < p.words.size(); i++){
                stm.setString(i + 1, p.words.get(i));
            }
            stm.executeUpdate();

            sb = new StringBuilder();
            sb.append("INSERT INTO links(pageid, link) " +
                    "VALUES ");
            for(int i = 0; i < p.links.size(); i++){
                if(i != 0) sb.append(", ");
                sb.append("(").append(insertId).append(", ?)");
            }
            sb.append(";");
            query = sb.toString();
            stm = connect.prepareStatement(query);
            for(int i = 0; i < p.links.size(); i++){
                stm.setString(i + 1, p.links.get(i));
            }
            stm.executeUpdate();

            System.out.println("Inserted page " + p.url + " with id " + insertId);
        } catch (SQLException e) {
            System.out.println("Insert page failed. Error message: " + e);
            System.out.println("Detail: " + e.getCause());
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
