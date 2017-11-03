package isrl.byu.edu.storage;
import java.io.Serializable;

public class FileTuple implements Serializable {
    private String filename;
    private byte[] data;

    public String getFileName() { return filename; }
    public byte[] getData()
    {
        return data;
    }
    public long getFileSize()
    {
        return data.length;
    }

    public FileTuple(String filename, byte[] data) {
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
        FileTuple otherFileTuple = (FileTuple) other;
        if (this.getFileName() != otherFileTuple.getFileName()) {
            return false;
        }

        if (this.getFileSize() != otherFileTuple.getFileSize())
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
