package isrl.byu.edu.bundle;

import java.net.ConnectException;
import java.util.*;

public class FileToBundleMapper {

    private HashSet<String> dirtyFileMappings = new HashSet<String>();
    private HashSet<String> dirtyBundleMappings = new HashSet<>();
    private HashSet<String> bundlesToBeDeleted = new HashSet<>();
    private HashMap<String, String> fileToBundleID = new HashMap<>();
    private HashMap<String, HashSet<String>>  bundleIDToFiles = new HashMap<>();

    public String getBundleID(String filename)
    {
        return fileToBundleID.get(filename);
    }

    public HashSet<String> getFilesInBundle(String bundleID)
    {
        HashSet<String> filesInBundle = bundleIDToFiles.get(bundleID);
        return filesInBundle;
    }
    private HashSet<String> addFileBundleMetadata(String bundleID, String filename)
    {
        HashSet<String> filesInBundle = bundleIDToFiles.get(bundleID);
        if(filesInBundle == null)
        {
            filesInBundle = new HashSet<>();
            bundleIDToFiles.put(bundleID, filesInBundle);
        }
        filesInBundle.add(filename);
        return filesInBundle;
    }
    public HashSet<String> getAndClearDeadBundleIDs()
    {
        HashSet<String> bundlesToBeDeletedCopy = (HashSet<String>)bundlesToBeDeleted.clone();
        dirtyBundleMappings.clear();
        return bundlesToBeDeletedCopy;
    }

    public HashSet<String> getAndClearDirtyBundleMappings()
    {
        HashSet<String> dirtyBundleMappingsCopy = (HashSet<String>)dirtyBundleMappings.clone();
        dirtyBundleMappings.clear();
        return dirtyBundleMappingsCopy;
    }

    public HashSet<String> getAndClearDirtyFileMappings()
    {
        HashSet<String> dirtyFileMappingsCopy = (HashSet<String>)dirtyFileMappings.clone();
        dirtyFileMappings.clear();
        return dirtyFileMappingsCopy;
    }

    public boolean remapBundle(Bundle newBundle)
    {
        if(newBundle == null)
        {
            return false;
        }

        for (BundleFileData bundleFileData:
                newBundle.getFiles()) {

            String oldBundleID = remapFile(bundleFileData.getFileName(), newBundle.getBundleID());
            dirtyBundleMappings.add(oldBundleID);
        }
        dirtyBundleMappings.add(newBundle.getBundleID());

        return true;
    }

    //1. set the file to bundle mapping
    //2. set the bundle to file mapping
    //3. remove the old bundle to file mapping and queue it for deletion if needed
    //4. return the old bundle Id
    private String remapFile(String filename, String bundleID)
    {
        String oldBundleID = fileToBundleID.put(filename, bundleID);
        if(oldBundleID == bundleID)
        {
            //its the same bundle
            return oldBundleID;
        }

        dirtyFileMappings.add(filename);

        if(oldBundleID != null)
        {
            HashSet<String> filesInOldBundle = getFilesInBundle(oldBundleID);
            filesInOldBundle.remove(filename);

            if(filesInOldBundle.size() == 0)
            {
                bundlesToBeDeleted.add(oldBundleID);
                dirtyBundleMappings.remove(oldBundleID);
                bundleIDToFiles.remove(oldBundleID);
            }
        }
        addFileBundleMetadata(bundleID, filename);

        return oldBundleID;
    }

}
