package isrl.byu.edu;

public class BundleFileData {
    String filename;
    byte[] data;

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
}
