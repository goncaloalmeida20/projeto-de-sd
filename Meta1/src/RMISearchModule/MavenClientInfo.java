package RMISearchModule;

import java.io.Serializable;

/**
 The ClientInfo MavenClientInfo represents a client's information in the RMISearchModule package.
 This class implements the Serializable interface to allow the objects to be serialized and transmitted
 over the network in RMI communication.
 */
public class MavenClientInfo implements Serializable {
    public int logged;
    public String username, password, s_id;

    /**
     * Constructs a new ClientInfo object with the specified ID, logged, username, and password.
     * @param s_id The ID of the client.
     * @param logged A flag that indicates whether the client is logged in or not.
     * @param username The username of the client.
     * @param password The password of the client.
     */
    public MavenClientInfo(String s_id, int logged, String username, String password) {
        this.s_id = s_id;
        this.logged = logged;
        this.username = username;
        this.password = password;
    }
}
