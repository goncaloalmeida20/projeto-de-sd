package RMIClient;

/**
 * The ClientAskedInfo class represents the information required by the RMI client
 * to send to the server for requesting information.
 */
public class ClientAskedInfo {
    public String username, password, url;
    public int termCount, n_page;

    public String[] terms;

    public ClientAskedInfo() {
        this.username = null;
        this.password = null;
        this.url = null;
        this.termCount = -1;
        this.n_page = -1;
        this.terms = null;
    }
}
