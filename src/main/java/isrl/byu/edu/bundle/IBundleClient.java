package isrl.byu.edu.bundle;

import isrl.byu.edu.storage.IStorage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public interface IBundleClient {

    boolean saveFile(byte[] bytes, String filename);
    byte[] readFile(String filename) throws NoSuchFileException, FileNotFoundException, ConnectException;

    boolean addRemoteLocation(IStorage iStorage);
    boolean flush();
}
