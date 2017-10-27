package isrl.byu.edu.storage;

import java.net.ConnectException;

public interface IStorage {
    String getID();
    int write(String filename, byte[] data) throws ConnectException;
    byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException;
    boolean delete(String filename) throws ConnectException;

    int writeMetadata(String key, String value) throws ConnectException;
    int deleteMetadata(String key) throws ConnectException;

}
