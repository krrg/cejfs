package isrl.byu.edu.bundle;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import com.sun.xml.internal.ws.encoding.soap.SerializationException;
import isrl.byu.edu.storage.FileTuple;

import java.io.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.UUID;

public class Bundle implements Serializable{

    private HashMap<String, FileTuple> files;
    private String bundleID;

    public String getBundleID() { return bundleID;}
    public Collection<FileTuple> getFiles() { return files.values(); }
    public FileTuple getFile(String filename){
        return files.get(filename);
    }

    private String generateBundleID() {
        //todo: should we make the bundleID a sha-hash of the files?
        return UUID.randomUUID().toString();
    }

    public Bundle(Collection<FileTuple> preCommittedFiles)
    {
        this.bundleID = generateBundleID();

        files = new HashMap<>();
        for (FileTuple fileTuple : preCommittedFiles) {
            files.put(fileTuple.getFileName(), fileTuple);
        }
    }

    public static byte[] serializeBundle(Bundle bundle) throws SerializationException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out;
        byte[] bundleBytes = null;
        try{
            out = new ObjectOutputStream(bos);
            out.writeObject(bundle);
            out.flush();
            bundleBytes = bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try{
                bos.close();
                return bundleBytes;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(bundleBytes == null)
        {
            throw new SerializationException("");
        }

        return bundleBytes;
    }
    public static Bundle deserializeBundle(byte[] data) throws DeserializationException {
        ByteArrayInputStream bis = new ByteArrayInputStream(data);
        ObjectInput in = null;
        Bundle bundle = null;
        try{
            in = new ObjectInputStream(bis);
            Object o = in.readObject();
            bundle = (Bundle)o;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try{
                if (in != null)
                {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if(bundle == null)
        {
            throw new DeserializationException("");
        }
        return bundle;
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
