package isrl.byu.edu.bundle;

import java.util.*;

public class FileToBundleMapper {

    private Queue<String> bundlesToBeDeleted = new LinkedList<String>();
    private HashMap<String, String> fileToBundleId = new HashMap<>();
    private HashMap<String, HashSet<String>>  bundleIdToFiles = new HashMap<>();

    public String popDeadBundleId()
    {
        return bundlesToBeDeleted.poll();
    }

    public boolean remapBundle(Bundle newBundle)
    {
        if(newBundle == null)
        {
            return false;
        }

        for (BundleFileData bundleFileData:
                newBundle.files) {

            remapFile(bundleFileData.filename, newBundle.bundleID);
        }

        return true;
    }

    //1. set the file to bundle mapping
    //2. set the bundle to file mapping
    //3. remove the old bundle to file mapping and queue it for deletion if needed
    //4. return the old bundle Id
    private String remapFile(String filename, String bundleId)
    {
        String oldBundleId = fileToBundleId.put(filename, bundleId);
        if(oldBundleId == bundleId)
        {
            //its the same bundle
            return oldBundleId;
        }
        if(oldBundleId != null)
        {
            HashSet<String> filesInOldBundle = getFilesInBundle(bundleId);
            filesInOldBundle.remove(filename);

            if(filesInOldBundle.size() == 0)
            {
                bundlesToBeDeleted.add(bundleId);
            }
        }

        HashSet<String> filesInBundle = getFilesInBundle(bundleId);
        filesInBundle.add(filename);

        return oldBundleId;
    }

    public String getBundleID(String filename)
    {
        return fileToBundleId.get(filename);
    }

    private HashSet<String> getFilesInBundle(String bundleId)
    {
        HashSet<String> filesInBundle = bundleIdToFiles.get(bundleId);
        if(filesInBundle == null)
        {
            filesInBundle = new HashSet<String>();
        }

        return filesInBundle;
    }
}
