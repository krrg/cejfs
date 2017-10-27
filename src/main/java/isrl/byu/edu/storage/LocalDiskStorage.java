package isrl.byu.edu.storage;

public class LocalDiskStorage implements IStorage {
    @Override
    public String getID() {
        return "local";
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
