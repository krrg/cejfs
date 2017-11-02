package isrl.byu.edu.bundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import isrl.byu.edu.storage.IStorage;
import isrl.byu.edu.storage.PendingBundleActions;
import isrl.byu.edu.utils.JSON;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class BundleClient implements IBundleClient {

    private FileToBundleMapper fileToBundleMapper = new FileToBundleMapper();
    private LinkedHashMap<String, BundleFileData> queuedFileSaves = new LinkedHashMap<>();
    private int queuedFileSavesByteSize = 0;

    private HashMap<String, Bundle> cachedBundles = new HashMap<>();
    private List<IStorage> storageLocations = new LinkedList<>();

    final int MEGABYTE = 1000000;
    final int MAX_FILES_PER_BUNDLE = 20;
    final double MAX_FILESIZE_PER_BUNDLE = MEGABYTE * 2.5;

    public BundleClient() {
    }
    public boolean addRemoteLocation(IStorage iStorage) {
        boolean alreadyExists = storageLocations.contains(iStorage);
        if(alreadyExists)
        {
            return false;
        }
        storageLocations.add(iStorage);
        return true;
    }


    /////////write data////////

    /**
       Returns false is the data is not flushed
       Returns true is the data is flushed into a bundle
     */
    @Override
    public boolean saveFile(byte[] bytes, String filename) {

        BundleFileData bundleFile = new BundleFileData(filename,bytes);
        if(queuedFileSaves.containsKey(filename)){

           BundleFileData oldBundleFileData = queuedFileSaves.remove(filename);
           queuedFileSavesByteSize -= oldBundleFileData.getFileSize();
        }

        queuedFileSaves.put(filename, bundleFile);
        queuedFileSavesByteSize += bundleFile.getFileSize();

        //todo: change this to be a file size instead of number of files or maybe and files?
        //if(queuedFileSaves.size() >= MAX_FILES_PER_BUNDLE)
        if(getTotalByteSizeOfQueuedFileSaves() >= MAX_FILESIZE_PER_BUNDLE) {
            flush();
            return true;
        }

        return false;
    }

    private int getTotalByteSizeOfQueuedFileSaves(){
        //todo: do something smarter?
        return queuedFileSavesByteSize;
    }

    /////////read data////////
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
        if(preCommittedFile != null) {
            return preCommittedFile.getData();
        }

        //get bundleID and cache related metadata
        String bundleID = fetchBundleID(filename);
        if(bundleID == null) {
            throw new NoSuchFileException("Could not find the metadata in any of the locations");
        }
        if(fileToBundleMapper.getBundleID(filename)==null) {
            //this means locally the meta data is new.
            //We should also get the bundleToFiles metadata
            HashSet<String> filesInBundle = fetchFilesInBundle(bundleID);
            if(filesInBundle == null) {
                throw new NoSuchFileException("Could not find the metadata in any of the locations");
            }
            if(fileToBundleMapper.getFilesInBundle(bundleID) == null)
            {
                fileToBundleMapper.setFilesInBundle(bundleID, filesInBundle);
            }

            fileToBundleMapper.remapFile(filename, bundleID);
        }

        //get bundle
        Bundle bundle = fetchBundle(bundleID, filename);
        if(bundle == null) {
            throw new FileNotFoundException("Could not find the bundle in any of the locations");
        }
        if(cachedBundles.get(bundleID)==null) {
            cachedBundles.put(bundleID, bundle);
        }
        BundleFileData committedFile = cachedBundles.get(bundleID).getFile(filename);

        return committedFile.getData();
    }
    private String fetchBundleID(String filename) {
        String fetchedBundleID = fileToBundleMapper.getBundleID(filename);

        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext() && fetchedBundleID == null) {
            IStorage storageLocation = it.next();
            try {
                fetchedBundleID = storageLocation.readMetadata(filename);
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
    private HashSet<String> fetchFilesInBundle(String bundleID) {
        HashSet<String> fileInBundle = fileToBundleMapper.getFilesInBundle(bundleID);

        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext() && fileInBundle == null) {
            IStorage storageLocation = it.next();
            try {
                String fileInBundleJson = storageLocation.readMetadata(bundleID);
                fileInBundle = JSON.JsonToSetOfStrings(fileInBundleJson);
            }
            catch (ConnectException e) {
                e.printStackTrace();
            }
            catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return fileInBundle;
    }

    private Bundle fetchBundle(String bundleID, String filename) {
        Bundle fetchedBundle = cachedBundles.get(bundleID);

        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext() && fetchedBundle == null) {
            IStorage storageLocation = it.next();
            try {
                byte[] bundleBytes = storageLocation.read(bundleID);
                fetchedBundle = Bundle.deserializeBundle(bundleBytes);
                if(fetchedBundle != null && fetchedBundle.getFile(filename) == null) {
                    throw new FileNotFoundException();
                }
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

    /////////create bundle and push it////////

    @Override
    public boolean flush() {

        if(queuedFileSaves.size() == 0) {
            return false;
        }

        //create bundle
        Bundle newBundle = createBundle();
        fileToBundleMapper.remapBundle(newBundle);

        //cache storage
        cacheNewBundle(newBundle);

        //remote storage
        queueChangesToStorage(newBundle);
        pushChangesToStorage();
        return true;
    }

    private Bundle createBundle() {
        Bundle newBundle = new Bundle(this.queuedFileSaves.values());
        this.queuedFileSaves.clear();
        this.queuedFileSavesByteSize =0;
        return newBundle;
    }

    /////////manage local cache////////
    private void cacheNewBundle(Bundle newBundle) {
        cachedBundles.put(newBundle.getBundleID(),newBundle);
    }
    private boolean cleanDeadCachedBundles(HashSet<String> deadBundleIDs) {
        boolean anyBundleDeleted = false;

        for (String deadBundleID: deadBundleIDs) {
            anyBundleDeleted = true;
            cachedBundles.remove(deadBundleID);
        }

        return anyBundleDeleted;
    }

    /////////queue and push changes to remote locations////////

    private void queueChangesToStorage(Bundle newBundle) {
        HashSet<String> dirtyBundleMappings = fileToBundleMapper.getAndClearDirtyBundleMappings();
        HashSet<String> dirtyFileMappings = fileToBundleMapper.getAndClearDirtyFileMappings();
        HashSet<String> deadBundleIDs = fileToBundleMapper.getAndClearDeadBundleIDs();

        cleanDeadCachedBundles(deadBundleIDs);
        Iterator<IStorage> it = storageLocations.iterator();
        while(it.hasNext()) {
            IStorage storageLocation = it.next();
            PendingBundleActions pendingBundleActions = storageLocation.getPendingBundleActions();
            pendingBundleActions.appendDirtyBundleMappings(dirtyBundleMappings);
            pendingBundleActions.appendDirtyFileMappings(dirtyFileMappings);
            pendingBundleActions.appendDeadBundleIDs(deadBundleIDs);
            pendingBundleActions.appendBundleReadyForUpload(newBundle);
        }
    }
    private boolean pushChangesToStorage() {
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
        PendingBundleActions pendingBundleActions = storageLocation.getPendingBundleActions();
        //update file mapping
        String dirtyFile = pendingBundleActions.popDirtyFileMapping();
        try {
            while (dirtyFile != null) {
                storageLocation.writeMetadata(dirtyFile, fileToBundleMapper.getBundleID(dirtyFile));
                dirtyFile = pendingBundleActions.popDirtyFileMapping();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActions.appendDirtyFileMapping(dirtyFile);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }
    private boolean pushDirtyBundleMappingsToStorageLocation(IStorage storageLocation){
        PendingBundleActions pendingBundleActions = storageLocation.getPendingBundleActions();
        //update bundle mapping
        String dirtyBundle = pendingBundleActions.popDirtyBundleMapping();
        try {
            while (dirtyBundle != null) {

                String jsonList = JSON.SetOfStringsToJson(fileToBundleMapper.getFilesInBundle(dirtyBundle));

                storageLocation.writeMetadata(dirtyBundle, jsonList);
                dirtyBundle = pendingBundleActions.popDirtyBundleMapping();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActions.appendDirtyBundleMapping(dirtyBundle);
            //stop trying to contact this storage device
            return false;
        } catch (JsonProcessingException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActions.appendDirtyBundleMapping(dirtyBundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }
    private boolean deleteDeadBundlesToStorageLocation(IStorage storageLocation){
        PendingBundleActions pendingBundleActions = storageLocation.getPendingBundleActions();
        //delete dead bundles
        String deadBundle= pendingBundleActions.popDeadBundleID();
        try {
            while (deadBundle != null) {

                storageLocation.delete(deadBundle);
                storageLocation.deleteMetadata(deadBundle);
                deadBundle = pendingBundleActions.popDeadBundleID();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActions.appendDeadBundleID(deadBundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }
    private boolean pushBundleToStorageLocation(IStorage storageLocation){
        PendingBundleActions pendingBundleActions = storageLocation.getPendingBundleActions();
        //delete dead bundles
        Bundle bundle = pendingBundleActions.popBundleReadyForUpload();
        try {
            while (bundle != null) {
                storageLocation.write(bundle.getBundleID(),Bundle.serializeBundle(bundle));
                bundle = pendingBundleActions.popBundleReadyForUpload();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingBundleActions.appendBundleReadyForUpload(bundle);
            //stop trying to contact this storage device
            return false;
        }
        return true;
    }



}
