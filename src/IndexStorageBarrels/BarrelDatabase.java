package IndexStorageBarrels;

import classes.Page;

import java.sql.*;
import java.util.HashMap;
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
    }

    /**
     * Starts the local Barrel Database, creating it if it doesn't exist yet
     */
    private void startDatabase(){
        Connection connect = null;
        Statement stm = null;

        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database...");
            connect = DriverManager.getConnection(url, user, password);

            stm = connect.createStatement();

            try {
                //try to create the database
                stm.executeUpdate("CREATE DATABASE " + dbName);
            }
            catch(SQLException e){
                //it already exists, continue normally
                System.out.println("Database " + dbName + " already exists");
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

    /**
     * Load the local Barrel Database tables, creating them if they don't exist yet
     */
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


            // Create Links table
            query = "CREATE TABLE IF NOT EXISTS links (" +
                    "pageid INT REFERENCES page(id) NOT NULL, " +
                    "link VARCHAR(" + VARCHAR_SIZE + ") NOT NULL, " +
                    "PRIMARY KEY (pageid, link)" +
                    ")";
            stm.executeUpdate(query);


            // Create Searches table
            query = "CREATE TABLE IF NOT EXISTS searches (" +
                    "type INTEGER NOT NULL, " +
                    "searchstring VARCHAR(" + VARCHAR_SIZE + ") NOT NULL, " +
                    "count INTEGER, " +
                    "PRIMARY KEY (type, searchstring) " +
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

    /**
     * Inserts a page into the local Barrel Database
     * @param p Page to insert
     * @param sender downloader that processed the page
     * @param seqNumber sequence number of the message where this page was sent
     */
    public void insertPage(Page p, int sender, int seqNumber){
        Connection connect = null;
        PreparedStatement stm = null;
        String query;
        StringBuilder sb;
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb , user, password);

            //insert page into page table, replacing it if it already exists
            query = "INSERT INTO page(url, title, citation, sender, seqnumber) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (url) DO UPDATE " +
                    "SET title = ?, " +
                    "   citation = ?, " +
                    "   sender = ?, " +
                    "   seqNumber = ?;";

            stm = connect.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            stm.setString(1, p.url.substring(0, Math.min(p.url.length(), VARCHAR_SIZE-1)));
            stm.setString(2, p.title.substring(0, Math.min(p.title.length(), VARCHAR_SIZE-1)));
            stm.setString(3, p.citation.substring(0, Math.min(p.citation.length(), VARCHAR_SIZE-1)));
            stm.setInt(4, sender);
            stm.setInt(5, seqNumber);
            stm.setString(6, p.title.substring(0, Math.min(p.title.length(), VARCHAR_SIZE-1)));
            stm.setString(7, p.citation.substring(0, Math.min(p.citation.length(), VARCHAR_SIZE-1)));
            stm.setInt(8, sender);
            stm.setInt(9, seqNumber);
            int insertedRows = stm.executeUpdate();
            System.out.println("Inserted rows:" + insertedRows + " url " + p.url.substring(0, Math.min(p.url.length(), VARCHAR_SIZE-1)));
            if(insertedRows > 0){
                ResultSet rs = stm.getGeneratedKeys();
                long insertId = -1;
                if(rs.next())
                    insertId = rs.getLong(1);
                if(insertId == -1) throw new RuntimeException("Error on returned generated keys");

                //if the page contains words, add them to the inverted index
                if(p.words.size() > 0){
                    sb = new StringBuilder();
                    sb.append("INSERT INTO invertedindex(term, pageid) " +
                            "VALUES ");
                    for(int i = 0; i < p.words.size(); i++){
                        if(i != 0) sb.append(", ");
                        sb.append("(?, ").append(insertId).append(")");
                    }
                    sb.append(" ON CONFLICT (term, pageid) DO NOTHING;");

                    query = sb.toString();
                    stm = connect.prepareStatement(query);
                    for(int i = 0; i < p.words.size(); i++){
                        stm.setString(i + 1, p.words.get(i).substring(0, Math.min(p.words.get(i).length(),
                                VARCHAR_SIZE-1)));
                    }
                    stm.executeUpdate();
                }

                //if the page contains links to other pages, add them to the link table
                if(p.links.size() > 0) {
                    sb = new StringBuilder();
                    sb.append("INSERT INTO links(pageid, link) " +
                            "VALUES ");
                    for (int i = 0; i < p.links.size(); i++) {
                        if (i != 0) sb.append(", ");
                        sb.append("(").append(insertId).append(", ?)");
                    }
                    sb.append(" ON CONFLICT (pageid, link) DO NOTHING;");
                    query = sb.toString();
                    stm = connect.prepareStatement(query);
                    for (int i = 0; i < p.links.size(); i++) {
                        stm.setString(i + 1, p.links.get(i).substring(0, Math.min(p.links.get(i).length(),
                                VARCHAR_SIZE - 1)));
                    }
                    stm.executeUpdate();
                }

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

    /**
     * Retrieves all pages processed by a downloader with sequence number higher than seqNumber
     * @param downloaderId Id of the downloader that processed the pages
     * @param seqNumber sequence number when it is being sent
     * @return a map with the pages retrieved and the sequence number of the message in which they arrived
     */
    public Map<Page, Integer> sendDownloaderPages(int downloaderId, int seqNumber){
        Connection connect = null;
        PreparedStatement stm = null;
        String query;
        Map<Page, Integer> pages = new HashMap<>();
        try {
            Class.forName("org.postgresql.Driver");

            System.out.println("Connecting to Database " + urldb + "...");
            connect = DriverManager.getConnection(urldb , user, password);

            //query to get the url, title, citation and seqNumber of the required pages
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
                //query to get the words from the required pages
                query = "SELECT term " +
                        "FROM invertedindex " +
                        "WHERE pageid=?;";

                stm = connect.prepareStatement(query);
                stm.setLong(1, pageid);
                ResultSet rsII = stm.executeQuery();
                while(rsII.next()){
                    p.words.add(rsII.getString("term"));
                }
                //query to get the links from the required pages
                query = "SELECT link " +
                        "FROM links " +
                        "WHERE pageid=?;";

                stm = connect.prepareStatement(query);
                stm.setLong(1, pageid);
                ResultSet rsL = stm.executeQuery();
                while(rsL.next()){
                    p.links.add(rsL.getString("link"));
                }
                //add the page and respective sequence number to the map
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

    /**
     * Adds a search to the "searches" table in the database.
     * @param type the type of the search
     * @param searchstring the search string
     * @throws RuntimeException if there's a problem with the JDBC driver
     */
    public void addSearch(int type, String searchstring) {
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            conn = DriverManager.getConnection(urldb, user, password);
            String sql = "INSERT INTO searches(type, searchstring, count) VALUES (?, ?, 1) " +
                    "ON CONFLICT (type, searchstring) DO UPDATE SET count = searches.count + 1";
            pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, type);
            pstmt.setString(2, searchstring);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Failed to insert search. Error message: " + e.getMessage());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (pstmt != null) {
                    pstmt.close();
                }
                if (conn != null) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.out.println("Error while closing connection with database. Error message: " + e.getMessage());
            }
        }
    }
}
