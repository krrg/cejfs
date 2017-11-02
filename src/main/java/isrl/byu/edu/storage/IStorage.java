package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public interface IStorage {
    String getID();

    PendingBundleActions getPendingBundleActions();

    int write(String filename, byte[] data) throws ConnectException;
    byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException;
    boolean delete(String filename) throws ConnectException;

    String writeMetadata(String key, String value) throws ConnectException;
    String readMetadata(String key) throws NoSuchFieldException, ConnectException;
    String deleteMetadata(String key) throws ConnectException;

}
