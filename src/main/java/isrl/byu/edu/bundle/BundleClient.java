package isrl.byu.edu.bundle;

import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import isrl.byu.edu.storage.AWSStorage;
import isrl.byu.edu.storage.IStorage;
import isrl.byu.edu.storage.LocalDiskStorage;
import isrl.byu.edu.utils.JSON;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class BundleClient implements IBundleClient {

    private FileToBundleMapper fileToBundleMapper = new FileToBundleMapper();
    private HashMap<String, BundleFileData> queuedFileSaves = new HashMap<>();
    private HashMap<String, Bundle> cachedBundles = new HashMap<>();

    private HashMap<String, PendingBundleActionsForStorage> pendingBundleActionsForStorageMap = new HashMap<>();
    private HashSet<IStorage> storageLocations = new HashSet<>();

    final int MAX_FILES_PER_BUNDLE = 20;

    IStorage localDisk;
    IStorage aws;

    public BundleClient()
    {
        localDisk = new LocalDiskStorage();
        aws = new AWSStorage();

        storageLocations.add(localDisk);
        storageLocations.add(aws);

        pendingBundleActionsForStorageMap.put(localDisk.getID(),new PendingBundleActionsForStorage());
        pendingBundleActionsForStorageMap.put(aws.getID(),new PendingBundleActionsForStorage());
    }

    /**
       Returns false is the data is not flushed
       Returns true is the data is flushed into a bundle
     */
    @Override
    public boolean saveFile(byte[] bytes, String filename) {

        BundleFileData bundleFile = new BundleFileData(filename,bytes);
        queuedFileSaves.put(filename, bundleFile);

        //todo: change this to be a file size instead of number of files or maybe and files?
        if(queuedFileSaves.size() >= MAX_FILES_PER_BUNDLE)
        {
            flush();
            return true;
        }

        return false;
    }

    /**
       1. Reading a file will check the queuedFileSaves for upto date data.
       2. Reading a file will check the cache
       3. If not in either, reading a file will attempt to fetch the bundle from remote locations
       4. If not available, the file is missing from AWS, or the filename has no meta data,
            then an exception will be called
     */
    @Override
    public byte[] readFile(String filename) throws NoSuchFileException, FileNotFoundException{

        //read from pre committed local data
        BundleFileData preCommittedFile = queuedFileSaves.get(filename);
        //todo: this assumes preCommittedFiles are always the most upto date.
        if(preCommittedFile != null)
        {
            return preCommittedFile.getData();
        }

        //get bundleID
        String bundleID = fetchBundleID(filename);
        if(bundleID == null)
        {
            throw new NoSuchFileException("Could not find the metadata in any of the locations");
        }
        if(fileToBundleMapper.getBundleID(filename)==null)
        {
            //todo: update cache with new data
        }

        //get bundle
        Bundle bundle = fetchBundle(bundleID, filename);
        if(bundle == null)
        {
            throw new FileNotFoundException("Could not find the bundle in any of the locations");
        }

        BundleFileData committedFile = cachedBundles.get(bundleID).getFile(filename);

        return committedFile.getData();
    }

    private String fetchBundleID(String filename)
    {
        String fetchedBundleID = fileToBundleMapper.getBundleID(filename);

        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext() && fetchedBundleID == null) {
            IStorage storageLocation = it.next();
            try {
                String bundleID = storageLocation.readMetadata(filename);
                if(bundleID != null)
                {
                    fetchedBundleID = bundleID;
                }
            }
            catch (ConnectException e) {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return fetchedBundleID;
    }

    private Bundle fetchBundle(String bundleID, String filename)
    {
        Bundle fetchedBundle = cachedBundles.get(bundleID);

        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext() && fetchedBundle == null) {
            IStorage storageLocation = it.next();
            try {
                byte[] bundleBytes = storageLocation.read(bundleID);
                Bundle bundle = Bundle.deserializeBundle(bundleBytes);
                if(bundle != null && bundle.getFile(filename) == null) {
                    throw new FileNotFoundException();
                }
                fetchedBundle = bundle;
            } catch (DeserializationException e) {
                e.printStackTrace();
            } catch (ConnectException e) {
                e.printStackTrace();
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (NoSuchFileException e) {
                e.printStackTrace();
            }
        }
        return fetchedBundle;
    }

    @Override
    public boolean flush() {

        Bundle newBundle = createBundle();
        fileToBundleMapper.remapBundle(newBundle);

        cacheNewBundle(newBundle);
        HashSet<String> dirtyBundleMappings = fileToBundleMapper.getAndClearDirtyBundleMappings();
        HashSet<String> dirtyFileMappings = fileToBundleMapper.getAndClearDirtyFileMappings();
        HashSet<String> deadBundleIDs = fileToBundleMapper.getAndClearDeadBundleIDs();

        cleanDeadCachedBundles(deadBundleIDs);

        for (String storageID: pendingBundleActionsForStorageMap.keySet()) {
            PendingBundleActionsForStorage pendingBundleActionsForStorage = pendingBundleActionsForStorageMap.get(storageID);
            pendingBundleActionsForStorage.appendDirtyBundleMappings(dirtyBundleMappings);
            pendingBundleActionsForStorage.appendDirtyFileMappings(dirtyFileMappings);
            pendingBundleActionsForStorage.appendDeadBundleIDs(deadBundleIDs);
            pendingBundleActionsForStorage.appendBundleReadyForUpload(newBundle);
        }

        pushChangesToStorageLocations();

        return true;
    }

    private Bundle createBundle()
    {
        String bundleID = generateBundleID();
        Bundle newBundle = new Bundle(bundleID, this.queuedFileSaves.values());
        this.queuedFileSaves.clear();
        return newBundle;
    }

    private String generateBundleID()
    {
        //todo: should we make the bundleID a sha-hash of the files?
        return UUID.randomUUID().toString();
    }

    private void cacheNewBundle(Bundle newBundle)
    {
        cachedBundles.put(newBundle.getBundleID(),newBundle);
    }

    private boolean cleanDeadCachedBundles(HashSet<String> deadBundleIDs)
    {
        boolean anyBundleDeleted = false;

        for (String deadBundleID: deadBundleIDs) {
            anyBundleDeleted = true;
            cachedBundles.remove(deadBundleID);
        }

        return anyBundleDeleted;
    }

    private boolean pushChangesToStorageLocations()
    {
        boolean everythingFinished = true;
        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext()) {
            IStorage storageLocation = it.next();
            if(!pushDirtyFileMappingsToStorageLocation(storageLocation))
            {
                //if we lose connection, lets stop trying this storageLocation
                everythingFinished = false;
                continue;
            }
            if(!pushDirtyBundleMappingsToStorageLocation(storageLocation))
            {
                //if we lose connection, lets stop trying this storageLocation
                everythingFinished = false;
                continue;
            }
            if(!deleteDeadBundlesToStorageLocation(storageLocation))
            {
                //if we lose connection, lets stop trying this storageLocation
                everythingFinished = false;
                continue;
            }
            if(!pushBundleToStorageLocation(storageLocation))
            {
                //if we lose connection, lets stop trying this storageLocation
                everythingFinished = false;
                continue;
            }
        }

        return everythingFinished;
    }
    private boolean pushDirtyFileMappingsToStorageLocation(IStorage storageLocation){
        PendingBundleActionsForStorage pendingBundleActionsForStorage = pendingBundleActionsForStorageMap.get(storageLocation.getID());
        //update file mapping
        String dirtyFile = pendingBundleActionsForStorage.popDirtyFileMapping();
        try {
            while (dirtyFile != null) {
                storageLocation.writeMetadata(dirtyFile, fileToBundleMapper.getBundleID(dirtyFile));
                dirtyFile = pendingBundleActionsForStorage.popDirtyFileMapping();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActionsForStorage.appendDirtyFileMapping(dirtyFile);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }

    private boolean pushDirtyBundleMappingsToStorageLocation(IStorage storageLocation){
        PendingBundleActionsForStorage pendingBundleActionsForStorage = pendingBundleActionsForStorageMap.get(storageLocation.getID());
        //update bundle mapping
        String dirtyBundle = pendingBundleActionsForStorage.popDirtyBundleMapping();
        try {
            while (dirtyBundle != null) {

                String jsonList = JSON.CollectionToJson(fileToBundleMapper.getFilesInBundle(dirtyBundle));

                storageLocation.writeMetadata(dirtyBundle, jsonList);
                dirtyBundle = pendingBundleActionsForStorage.popDirtyBundleMapping();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActionsForStorage.appendDirtyBundleMapping(dirtyBundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }

    private boolean deleteDeadBundlesToStorageLocation(IStorage storageLocation){
        PendingBundleActionsForStorage pendingBundleActionsForStorage = pendingBundleActionsForStorageMap.get(storageLocation.getID());
        //delete dead bundles
        String deadBundle= pendingBundleActionsForStorage.popDeadBundleID();
        try {
            while (deadBundle != null) {

                storageLocation.delete(deadBundle);
                storageLocation.deleteMetadata(deadBundle);
                deadBundle = pendingBundleActionsForStorage.popDeadBundleID();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActionsForStorage.appendDeadBundleID(deadBundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }

    private boolean pushBundleToStorageLocation(IStorage storageLocation){
        PendingBundleActionsForStorage pendingBundleActionsForStorage = pendingBundleActionsForStorageMap.get(storageLocation.getID());
        //delete dead bundles
        Bundle bundle = pendingBundleActionsForStorage.popBundleReadyForUpload();
        try {
            while (bundle != null) {

                storageLocation.write(bundle.getBundleID(),Bundle.serializeBundle(bundle));
                bundle = pendingBundleActionsForStorage.popBundleReadyForUpload();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActionsForStorage.appendBundleReadyForUpload(bundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }



}
