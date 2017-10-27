package isrl.byu.edu.bundle;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;

import java.util.Collection;
import java.util.HashMap;

public class Bundle {

    private HashMap<String, BundleFileData> files;
    private String bundleID;

    public String getBundleID() { return bundleID;}
    public Collection<BundleFileData> getFiles() { return files.values(); }
    public BundleFileData getFile(String filename){
        return files.get(filename);
    }

    public static byte[] serializeBundle(Bundle bundle) throws SerializationException
    {
        throw new SerializationException("");
        //todo:
        return null;
    }

    public static Bundle deserializeBundle(byte[] data) throws DeserializationException
    {
        throw new DeserializationException("");
        //todo:
        return null;
    }

    public Bundle(String bundleID, Collection<BundleFileData> preCommittedFiles)
    {
        this.bundleID = bundleID;

        files = new HashMap<>();
        for (BundleFileData bundleFileData: preCommittedFiles) {
            files.put(bundleFileData.getFileName(), bundleFileData);
        }
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
        Bundle otherBundle = (Bundle) other;
        if (this.getBundleID() != otherBundle.getBundleID()) {
            return false;
        }

        if (this.getFiles().size() != otherBundle.getFiles().size())
        {
            return false;
        }

        Object[] myFiles = this.getFiles().toArray();
        Object[] otherFiles = otherBundle.getFiles().toArray();

        for (int i =0; i< this.getFiles().size(); i++)
        {
            if(myFiles[i] != otherFiles[i])
            {
                return false;
            }
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getBundleID().hashCode();
    }
}
