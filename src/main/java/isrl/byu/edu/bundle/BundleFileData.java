package isrl.byu.edu.bundle;
import java.sql.Timestamp;

public class BundleFileData {
    private String filename;
    private byte[] data;
    private Timestamp lastModified;

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
    public Timestamp getLastModified() { return lastModified; }

    public BundleFileData(String filename, byte[] data)
    {
        this.filename = filename;
        this.data = data;
        this.lastModified = new Timestamp(System.currentTimeMillis());
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

        if (this.getLastModified() != otherBundleFileData.getLastModified())
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
