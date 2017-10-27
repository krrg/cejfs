package isrl.byu.edu.bundle;
import java.io.Serializable;

public class BundleFileData implements Serializable {
    private String filename;
    private byte[] data;

    public String getFileName()
    {
        return filename;
    }
    public byte[] getData()
    {
        return data;
    }
    public long getFileSize()
    {
        return data.length;
    }

    public BundleFileData(String filename, byte[] data)
    {
        this.filename = filename;
        this.data = data;
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
        BundleFileData otherBundleFileData = (BundleFileData) other;
        if (this.getFileName() != otherBundleFileData.getFileName()) {
            return false;
        }

        if (this.getFileSize() != otherBundleFileData.getFileSize())
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getFileName().hashCode();
    }

}
