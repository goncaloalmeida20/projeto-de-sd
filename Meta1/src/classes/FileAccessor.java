package classes;

import RMISearchModule.ServerInfo;

import java.io.*;
import java.nio.channels.*;

/**
 A class for accessing a file and writing/reading a ServerInfo object to/from it
 */
public class FileAccessor {
    private final File file;
    private final FileOutputStream outputStream;
    private final FileInputStream inputStream;
    private FileLock fileLock;

    /**
     * Constructs a FileAccessor for a given file name
     * @param fileName the name of the file to access
     * @throws IOException if the file doesn't exist or cannot be accessed
     */
    public FileAccessor(String fileName) throws IOException {
        this.file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + fileName);
        }
        this.outputStream = new FileOutputStream(file);
        this.inputStream = new FileInputStream(file);
    }

    /**
     * Writes a ServerInfo object to the file.
     * @param object the ServerInfo object to write
     * @throws IOException if an I/O error occurs
     */
    public synchronized void write(ServerInfo object) throws IOException {
        fileLock = outputStream.getChannel().lock();
        try {
            ObjectOutputStream out = new ObjectOutputStream(outputStream);
            out.writeObject(object);
            out.flush();
        } finally {
            fileLock.release();
        }
    }

    /**
     * Reads a ServerInfo object from the file.
     * @return the ServerInfo object read from the file
     * @throws IOException            if an I/O error occurs
     * @throws ClassNotFoundException if the class of the serialized ServerInfo object cannot be found
     */
    public synchronized ServerInfo read() throws IOException, ClassNotFoundException {
        fileLock = inputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            return (ServerInfo) in.readObject();
        } finally {
            fileLock.release();
        }
    }

    /**
     * Closes the input and output streams
     * @throws IOException if an I/O error occurs
     */
    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
    }
}