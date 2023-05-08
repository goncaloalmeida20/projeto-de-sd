package IndexStorageBarrels;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * The Barrel class represents an instance of a storage barrel, that saves all the application data.
 * It provides multicast communication capabilities to synchronize the barrels and
 * recover from failures, as well as database access for persistency.
 */
public class Barrel{
    public static final String MULTICAST_ADDRESS = "224.0.1.0";
    public static final int MULTICAST_PORT = 5000;

    public static final String SYNC_MULTICAST_ADDRESS = "224.0.1.1";
    public static final int SYNC_MULTICAST_PORT = 5001;

    public static final int SEQ_NUMBER_DIFF_TOLERANCE = 3;
    Thread t;
    private static BarrelModule barrelModule;
    public static BarrelMulticastWorker bmw;

    public static BarrelMulticastRecovery bmr;
    public static BarrelMulticastReceiver bmrcv;

    public static InterBarrelSynchronizerReceiver ibsr;
    public static InterBarrelSynchronizerHelper ibsh;
    public static InterBarrelSynchronizerInserter ibsi;

    public static BarrelCleaner bc;

    public static BarrelDatabase bdb;

    /**
     * Initializes a new instance of a Barrel object, setting up its thread responsible for the RMI communication
     * @throws RemoteException if a remote exception occurs
     * @throws NotBoundException if a RMI communication exception occurs
     */
    public Barrel(int id) throws RemoteException, NotBoundException {
        barrelModule = new BarrelModule(id);
        t = new Thread(barrelModule);
        t.start();

        //String url = "jdbc:postgresql://localhost:5432/";
        //String user = "postgres";
        //String password = "postgres";
        //bdb = new BarrelDatabase(url, barrelModule.getId(), user, password);
    }

    /**
     * The main method creates a new instance of a Barrel object, initializes its communication components,
     * initializes its internal data structures and establishes a connection to the database
     * @param args the command-line arguments (not used)
     * @throws NotBoundException if a RMI communication exception occurs
     * @throws RemoteException if a remote exception occurs
     * */
    public static void main(String[] args) throws NotBoundException, RemoteException {
        int id = Integer.parseInt(args[0]);
        String url = "jdbc:postgresql://localhost:5432/";
        String user = "postgres";
        String password = "postgres";
        bdb = new BarrelDatabase(url, id, user, password);
        new Barrel(id);
        //int id = barrelModule.getId();
        bmrcv = new BarrelMulticastReceiver(id);
        bmr = new BarrelMulticastRecovery(id);
        bmw = new BarrelMulticastWorker(id);
        ibsr = new InterBarrelSynchronizerReceiver(id);
        ibsh = new InterBarrelSynchronizerHelper(id);
        ibsi = new InterBarrelSynchronizerInserter(id);
        bc = new BarrelCleaner(id);
        System.out.println("Barrel is ready");
    }
}