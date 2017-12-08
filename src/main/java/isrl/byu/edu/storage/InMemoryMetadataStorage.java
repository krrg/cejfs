package isrl.byu.edu.storage;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;

public class InMemoryMetadataStorage implements IMetadataStorage {

    private HashMap<String, String> metaDataMapper = new HashMap<>();
    private PendingMetadataActions pendingActions = new PendingMetadataActions();

    @Override
    public String getID() {
        return "memoryMetadata";
    }

    @Override
    public PendingMetadataActions getPendingActions() {
        return pendingActions;
    }

    @Override
    public String writeMetadata(String key, String value) throws ConnectException {
        return metaDataMapper.put(key, value);
    }

    @Override
    public String readMetadata(String key) throws NoSuchFieldException, ConnectException {
        if(!metaDataMapper.containsKey(key))
        {
            throw new NoSuchFieldException("No such field is coming to town! " + key);
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
        InMemoryMetadataStorage otherStorage = (InMemoryMetadataStorage) other;
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
