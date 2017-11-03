package isrl.byu.edu.bundle;

import isrl.byu.edu.storage.IDataStorage;
import isrl.byu.edu.storage.IMetadataStorage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public interface IBundleClient {

    boolean saveFile(byte[] bytes, String filename);
    byte[] readFile(String filename) throws NoSuchFileException, FileNotFoundException, ConnectException;

    boolean addDataLocation(IDataStorage iDataStorage);
    boolean addMetadataLocation(IMetadataStorage iMetadataStorage);
    boolean flush();
}
