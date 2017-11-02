package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;

public class InMemoryStorage implements IStorage {

    private HashMap<String, byte[]> cachedBundleBytes = new HashMap<>();
    private HashMap<String, String> metaDataMapper = new HashMap<>();
    private PendingBundleActions pendingBundleActions = new PendingBundleActions();

    @Override
    public String getID() {
        return "memory";
    }

    @Override
    public PendingBundleActions getPendingBundleActions() {
        return pendingBundleActions;
    }

    @Override
    public int write(String filename, byte[] data) throws ConnectException {
        cachedBundleBytes.put(filename, data);
        return data.length;
    }

    @Override
    public byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException {
        if(!metaDataMapper.containsKey(filename))
        {
            throw new NoSuchFileException("");
        }
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
    public String writeMetadata(String key, String value) throws ConnectException {
        return metaDataMapper.put(key, value);
    }

    @Override
    public String readMetadata(String key) throws NoSuchFieldException, ConnectException {
        if(!metaDataMapper.containsKey(key))
        {
            throw new NoSuchFieldException("");
        }
        return metaDataMapper.get(key);
    }

    @Override
    public String deleteMetadata(String key) throws ConnectException {
        return metaDataMapper.remove(key);
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
        InMemoryStorage otherStorage = (InMemoryStorage) other;
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
