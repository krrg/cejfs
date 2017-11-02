package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public class LocalDiskStorage implements IStorage {

    private PendingBundleActions pendingBundleActions = new PendingBundleActions();

    @Override
    public PendingBundleActions getPendingBundleActions() {
        return pendingBundleActions;
    }

    @Override
    public String getID() {
        return "local";
    }

    @Override
    public int write(String filename, byte[] data) throws ConnectException {
        return 0;
    }

    @Override
    public byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException {
        return new byte[0];
    }

    @Override
    public boolean delete(String filename) throws ConnectException {
        return false;
    }

    @Override
    public String writeMetadata(String key, String value) throws ConnectException {
        return null;
    }

    @Override
    public String readMetadata(String key) throws NoSuchFieldException, ConnectException {
        return null;
    }

    @Override
    public String deleteMetadata(String key) throws ConnectException {
        return null;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        LocalDiskStorage otherStorage = (LocalDiskStorage) other;
        if (this.getID() != otherStorage.getID()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getID().hashCode();
    }


}
