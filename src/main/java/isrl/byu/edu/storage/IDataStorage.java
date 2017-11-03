package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public interface IDataStorage {
    String getID();

    PendingDataActions getPendingActions();

    int write(String filename, byte[] data) throws ConnectException;
    byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException;
    boolean delete(String filename) throws ConnectException;
}
