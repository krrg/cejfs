package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public interface IMetadataStorage {
    String getID();

    PendingMetadataActions getPendingActions();

    String writeMetadata(String key, String value) throws ConnectException;
    String readMetadata(String key) throws NoSuchFieldException, ConnectException;
    String deleteMetadata(String key) throws ConnectException;

}
