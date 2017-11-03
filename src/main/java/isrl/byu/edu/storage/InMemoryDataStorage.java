package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;

public class InMemoryDataStorage implements IDataStorage {

    private HashMap<String, byte[]> cachedBundleBytes = new HashMap<>();
    private PendingDataActions pendingActions = new PendingDataActions();

    @Override
    public String getID() {
        return "memoryData";
    }

    @Override
    public PendingDataActions getPendingActions() {
        return pendingActions;
    }
    @Override
    public int write(String filename, byte[] data) throws ConnectException {
        cachedBundleBytes.put(filename, data);
        return data.length;
    }

    @Override
    public byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException {
        if(!cachedBundleBytes.containsKey(filename))
        {
            throw new FileNotFoundException();
        }

        return cachedBundleBytes.get(filename);
    }

    @Override
    public boolean delete(String filename) throws ConnectException {
        return cachedBundleBytes.remove(filename) != null;
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
        InMemoryDataStorage otherStorage = (InMemoryDataStorage) other;
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
