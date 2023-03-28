package RMISearchModule;

import java.io.Serializable;

public class ClientInfo implements Serializable {
    public int id, logged;
    public String username, password;

    public ClientInfo(int id, int logged, String username, String password) {
        this.id = id;
        this.logged = logged;
        this.username = username;
        this.password = password;
    }
}
