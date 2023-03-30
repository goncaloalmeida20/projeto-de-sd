package classes;

import java.io.Serializable;

/**
 The DownloaderAndBarrelInfo class represents information about a downloader or a barrel in the search module
 It implements the Serializable interface to allow objects of this class to be serialized and deserialized
 */
public class DownloaderAndBarrelInfo implements Serializable {
    public String ip;

    public int port, id;

    /**
     * Constructs a new DownloaderAndBarrelInfo object with the specified ip address, port number and the downloader/barrel id
     * @param ip  the ip address of the downloader/barrel
     * @param port  the port number which the downloader/barrel is listening
     * @param id  the ID of this downloader/barrel
     */
    public DownloaderAndBarrelInfo(String ip, int port, int id) {
        this.ip = ip;
        this.port = port;
        this.id = id;
    }
}
