package isrl.byu.edu.bundle;
import isrl.byu.edu.storage.FileTuple;

import java.util.*;

public class FileToBundleMapper {

    private HashSet<String> dirtyFileMappings = new HashSet<String>();
    private HashSet<String> dirtyBundleMappings = new HashSet<>();
    private HashSet<String> bundlesToBeDeleted = new HashSet<>();
    private HashMap<String, String> fileToBundleID = new HashMap<>();
    private HashMap<String, HashSet<String>>  bundleIDToFiles = new HashMap<>();

    public String getBundleID(String filename) {
        return fileToBundleID.get(filename);
    }

    public HashSet<String> getFilesInBundle(String bundleID) {
        HashSet<String> filesInBundle = bundleIDToFiles.get(bundleID);
        return filesInBundle;
    }
    public boolean setFilesInBundle(String bundleID, HashSet<String> filesInBundle) {
        boolean overwritten = bundleIDToFiles.containsKey(bundleID);
        bundleIDToFiles.put(bundleID, filesInBundle);
        return overwritten;
    }

    ////////Dirty data retrieval for pushing to remote servers ////////
    public HashSet<String> getAndClearDeadBundleIDs() {
        HashSet<String> bundlesToBeDeletedCopy = (HashSet<String>)bundlesToBeDeleted.clone();
        dirtyBundleMappings.clear();
        return bundlesToBeDeletedCopy;
    }

    public HashSet<String> getAndClearDirtyBundleMappings() {
        HashSet<String> dirtyBundleMappingsCopy = (HashSet<String>)dirtyBundleMappings.clone();
        dirtyBundleMappings.clear();
        return dirtyBundleMappingsCopy;
    }

    public HashSet<String> getAndClearDirtyFileMappings() {
        HashSet<String> dirtyFileMappingsCopy = (HashSet<String>)dirtyFileMappings.clone();
        dirtyFileMappings.clear();
        return dirtyFileMappingsCopy;
    }

    ////////Remaping bundle and files ////////

    public boolean remapBundle(Bundle newBundle){
        if(newBundle == null)
        {
            return false;
        }

        for (FileTuple fileTuple :
                newBundle.getFiles()) {

            remapFile(fileTuple.getFileName(), newBundle.getBundleID());
        }
        dirtyBundleMappings.add(newBundle.getBundleID());

        return true;
    }

    //1. set the file to bundle mapping
    //2. set the bundle to file mapping
    //3. removeChild the old bundle to file mapping and queue it for deletion if needed
    //4. return the old bundle Id
    public String remapFile(String filename, String bundleID) {
        String oldBundleID = fileToBundleID.put(filename, bundleID);
        if(oldBundleID != null) {
            dirtyBundleMappings.add(oldBundleID);
        }

        if(oldBundleID == bundleID)
        {
            //its the same bundle
            return oldBundleID;
        }

        dirtyFileMappings.add(filename);
        cleanOldBundle(oldBundleID, filename);
        addFileBundleMetadata(bundleID, filename);

        return oldBundleID;
    }

    private HashSet<String> addFileBundleMetadata(String bundleID, String filename) {
        HashSet<String> filesInBundle = bundleIDToFiles.get(bundleID);
        if(filesInBundle == null)
        {
            filesInBundle = new HashSet<>();
            bundleIDToFiles.put(bundleID, filesInBundle);
        }
        filesInBundle.add(filename);
        return filesInBundle;
    }

    private boolean cleanOldBundle(String oldBundleID, String filename) {
        boolean beginOldBundleDeletion =false;
        if(oldBundleID != null)
        {
            HashSet<String> filesInOldBundle = getFilesInBundle(oldBundleID);
            filesInOldBundle.remove(filename);

            if(filesInOldBundle.size() == 0)
            {
                beginOldBundleDeletion =true;
                bundlesToBeDeleted.add(oldBundleID);
                dirtyBundleMappings.remove(oldBundleID);
                bundleIDToFiles.remove(oldBundleID);

            }
        }
        return beginOldBundleDeletion;
    }

}
