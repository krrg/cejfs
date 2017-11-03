package isrl.byu.edu.storage;
import java.io.Serializable;

public class MetadataTuple implements Serializable {
    private String key;
    private String value;

    public String getKey() { return key; }
    public String getValue()
    {
        return value;
    }

    public MetadataTuple(String key, String value) {
        this.key = key;
        this.value = value;
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
        MetadataTuple otherFileTuple = (MetadataTuple) other;
        if (this.getKey() != otherFileTuple.getKey()) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getKey().hashCode();
    }

}
