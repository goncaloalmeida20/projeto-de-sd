package classes;

import RMISearchModule.ServerInfo;

import java.io.*;
import java.nio.channels.*;

public class FileAccessor {
    private final File file;
    private final FileOutputStream outputStream;
    private final FileInputStream inputStream;
    private FileLock fileLock;

    public FileAccessor(String fileName) throws IOException {
        this.file = new File(fileName);
        if (!file.exists()) {
            throw new FileNotFoundException("File does not exist: " + fileName);
        }
        this.outputStream = new FileOutputStream(file);
        this.inputStream = new FileInputStream(file);
    }

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

    public synchronized ServerInfo read() throws IOException, ClassNotFoundException {
        fileLock = inputStream.getChannel().lock(0L, Long.MAX_VALUE, true);
        try {
            ObjectInputStream in = new ObjectInputStream(inputStream);
            return (ServerInfo) in.readObject();
        } finally {
            fileLock.release();
        }
    }

    public void close() throws IOException {
        outputStream.close();
        inputStream.close();
    }
}