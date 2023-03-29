package IndexStorageBarrels;

import classes.Page;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BarrelDatabase {
    public static final int VARCHAR_SIZE = 255;
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
        /*Page p = new Page();
        p.url = "testeurl";
        p.title = "testetitle";
        p.citation = "testecitation";
        p.words.add("teste1");
        p.words.add("teste2");
        p.words.add("teste3");
        p.links.add("www.ph.com");
        p.links.add("www.xv.com");
        insertPage(p);*/
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
                System.out.println("Database " + dbName + " already exists");
                //System.out.println("Error message: " + e.getMessage());
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
                    "url VARCHAR(" + VARCHAR_SIZE + ") UNIQUE NOT NULL, " +
                    "title VARCHAR(" + VARCHAR_SIZE + ") NOT NULL, " +
                    "citation VARCHAR("+ VARCHAR_SIZE + ") NOT NULL, " +
                    "sender INT, " +
                    "seqnumber INT, " +
                    "PRIMARY KEY (id) " +
                    ")";
            stm.executeUpdate(query);

            query = "CREATE UNIQUE INDEX IF NOT EXISTS urlindex ON page (url);";
            stm.executeUpdate(query);

            // Create InvertedIndex table
            query = "CREATE TABLE IF NOT EXISTS invertedindex (" +
                    "term VARCHAR(" + VARCHAR_SIZE + ") NOT NULL, " +
                    "pageid INT REFERENCES page(id) NOT NULL, " +
                    "PRIMARY KEY (term, pageid)" +
                    ")";
            stm.executeUpdate(query);

            /*query = "CREATE UNIQUE INDEX IF NOT EXISTS invertedindex ON invertedindex (term, pageid)";
            stm.executeUpdate(query);*/

            // Create Links table
            query = "CREATE TABLE IF NOT EXISTS links (" +
                    "pageid INT REFERENCES page(id) NOT NULL, " +
                    "link VARCHAR(" + VARCHAR_SIZE + ") NOT NULL, " +
                    "PRIMARY KEY (pageid, link)" +
                    ")";
            stm.executeUpdate(query);

            /*query = "CREATE UNIQUE INDEX IF NOT EXISTS links ON links (pageid, link)";
            stm.executeUpdate(query);*/
            
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

    public void insertPage(Page p, int sender, int seqNumber){
        Connection connect = null;
        PreparedStatement stm = null;
        String query;
        StringBuilder sb;
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb , user, password);

            query = "INSERT INTO page(url, title, citation, sender, seqnumber) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (url) DO NOTHING;";

            stm = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, p.url.substring(0, Math.min(p.url.length(), VARCHAR_SIZE-1)));
            stm.setString(2, p.title.substring(0, Math.min(p.title.length(), VARCHAR_SIZE-1)));
            stm.setString(3, p.citation.substring(0, Math.min(p.citation.length(), VARCHAR_SIZE-1)));
            stm.setInt(4, sender);
            stm.setInt(5, seqNumber);
            int insertedRows = stm.executeUpdate();
            System.out.println("Inserted rows:" + insertedRows + " url " + p.url.substring(0, Math.min(p.url.length(), VARCHAR_SIZE-1)));
            if(insertedRows > 0){
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
                /*sb.append(" ON CONFLICT (term, pageid) DO UPDATE " +
                    "SET term = excluded.term, " +
                    "pageid = excluded.pageid;");*/

                query = sb.toString();
                stm = connect.prepareStatement(query);
                for(int i = 0; i < p.words.size(); i++){
                    stm.setString(i + 1, p.words.get(i).substring(0, Math.min(p.words.get(i).length(),
                            VARCHAR_SIZE-1)));
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
                /*sb.append(" ON CONFLICT (pageid, link) DO UPDATE " +
                    "SET pageid = excluded.pageid, " +
                    "link = excluded.link;");*/
                query = sb.toString();
                stm = connect.prepareStatement(query);
                for(int i = 0; i < p.links.size(); i++){
                    stm.setString(i + 1, p.links.get(i).substring(0, Math.min(p.links.get(i).length(),
                            VARCHAR_SIZE-1)));
                }
                stm.executeUpdate();

                System.out.println("Inserted page " + p.url + " with id " + insertId);
            }
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

    public Map<Page, Integer> sendDownloaderPages(int downloaderId, int seqNumber){
        Connection connect = null;
        PreparedStatement stm = null;
        String query;
        Map<Page, Integer> pages = new HashMap<>();
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb , user, password);

            query = "SELECT id, url, title, citation, seqnumber " +
                    "FROM page " +
                    "WHERE sender=? AND seqnumber>=?;";

            stm = connect.prepareStatement(query);
            stm.setInt(1, downloaderId);
            stm.setInt(2, seqNumber);
            ResultSet rs = stm.executeQuery();
            while(rs.next()){
                Page p = new Page();
                long pageid = rs.getLong("id");
                p.url = rs.getString("url");
                p.title = rs.getString("title");
                p.citation = rs.getString("citation");
                int currentSeqNumber = rs.getInt("seqnumber");
                query = "SELECT term " +
                        "FROM invertedindex " +
                        "WHERE pageid=?;";

                stm = connect.prepareStatement(query);
                stm.setLong(1, pageid);
                ResultSet rsII = stm.executeQuery();
                while(rsII.next()){
                    p.words.add(rsII.getString("term"));
                }

                query = "SELECT link " +
                        "FROM links " +
                        "WHERE pageid=?;";

                stm = connect.prepareStatement(query);
                stm.setLong(1, pageid);
                ResultSet rsL = stm.executeQuery();
                while(rsL.next()){
                    p.links.add(rsL.getString("link"));
                }
                pages.put(p, currentSeqNumber);
            }

        } catch (SQLException e) {
            System.out.println("Send downloader pages failed. Error message: " + e);
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
        return pages;
    }
}
