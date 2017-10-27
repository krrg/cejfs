package isrl.byu.edu.bundle;

import java.util.HashSet;

public class PendingBundleActionsForStorage {

    private HashSet<String> dirtyBundleMappings = new HashSet<>();
    private HashSet<String> dirtyFileMappings = new HashSet<>();
    private HashSet<String> deadBundleIDs = new HashSet<>();
    private HashSet<Bundle> bundlesReadyForUpload = new HashSet<>();

    public void appendDirtyBundleMapping(String dirtyBundleMapping)
    {
        this.dirtyBundleMappings.add(dirtyBundleMapping);
    }
    public void appendDirtyBundleMappings(HashSet<String> dirtyBundleMappings)
    {
        this.dirtyBundleMappings.addAll(dirtyBundleMappings);
    }

    public String popDirtyBundleMapping()
    {
        if(this.dirtyBundleMappings.iterator().hasNext()) {
            String mapping = this.dirtyBundleMappings.iterator().next();
            this.dirtyBundleMappings.remove(mapping);
            return mapping;
        }
        return null;
    }


    public void appendDirtyFileMapping(String dirtyFileMapping)
    {
        this.dirtyFileMappings.add(dirtyFileMapping);
    }

    public void appendDirtyFileMappings(HashSet<String> dirtyFileMappings)
    {
        this.dirtyFileMappings.addAll(dirtyFileMappings);

    }

    public String popDirtyFileMapping()
    {
        if(this.dirtyFileMappings.iterator().hasNext()) {
            String mapping = this.dirtyFileMappings.iterator().next();
            this.dirtyFileMappings.remove(mapping);
            return mapping;
        }
        return null;
    }

    public void appendDeadBundleID(String deadBundleID)
    {
        this.deadBundleIDs.add(deadBundleID);

    }

    public void appendDeadBundleIDs(HashSet<String> deadBundleIDs)
    {
        this.deadBundleIDs.addAll(deadBundleIDs);

    }

    public String popDeadBundleID()
    {
        if(this.deadBundleIDs.iterator().hasNext()) {
            String mapping = this.deadBundleIDs.iterator().next();
            this.deadBundleIDs.remove(mapping);
            return mapping;
        }
        return null;
    }

    public void appendBundleReadyForUpload(Bundle bundleReadyForUpload)
    {
        this.bundlesReadyForUpload.add(bundleReadyForUpload);

    }
    public void appendBundlesReadyForUpload(HashSet<Bundle> bundlesReadyForUpload)
    {
        this.bundlesReadyForUpload.addAll(bundlesReadyForUpload);
    }

    public Bundle popBundleReadyForUpload()
    {
        if(this.bundlesReadyForUpload.iterator().hasNext()) {
            Bundle bundle = this.bundlesReadyForUpload.iterator().next();
            this.bundlesReadyForUpload.remove(bundle);
            return bundle;
        }
        return null;
    }
}
