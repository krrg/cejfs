package isrl.byu.edu.bundle;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sun.xml.internal.ws.encoding.soap.DeserializationException;
import isrl.byu.edu.storage.*;
import isrl.byu.edu.utils.JSON;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class BundleClient implements IBundleClient {

    private FileToBundleMapper fileToBundleMapper = new FileToBundleMapper();
    private LinkedHashMap<String, FileTuple> queuedFileSaves = new LinkedHashMap<>();
    private int queuedFileSavesByteSize = 0;

    private HashMap<String, Bundle> cachedBundles = new HashMap<>();
    private List<IDataStorage> dataLocations = new LinkedList<>();
    private List<IMetadataStorage> metadataLocations = new LinkedList<>();

    final int MEGABYTE = 1000000;
    final int MAX_FILES_PER_BUNDLE = 20;
    final double MAX_FILESIZE_PER_BUNDLE = MEGABYTE * 2.5;

    public BundleClient() {
    }
    public boolean addDataLocation(IDataStorage iDataStorage) {
        boolean alreadyExists = dataLocations.contains(iDataStorage);
        if(alreadyExists)
        {
            return false;
        }
        dataLocations.add(iDataStorage);
        return true;
    }
    public boolean addMetadataLocation(IMetadataStorage iMetadataStorage) {
        boolean alreadyExists = metadataLocations.contains(iMetadataStorage);
        if(alreadyExists)
        {
            return false;
        }
        metadataLocations.add(iMetadataStorage);
        return true;
    }

    /////////write data////////

    /**
       Returns false is the data is not flushed
       Returns true is the data is flushed into a bundle
     */
    @Override
    public boolean saveFile(byte[] bytes, String filename) {

        FileTuple bundleFile = new FileTuple(filename,bytes);
        if(queuedFileSaves.containsKey(filename)){

           FileTuple oldFileTuple = queuedFileSaves.remove(filename);
           queuedFileSavesByteSize -= oldFileTuple.getFileSize();
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
    public byte[] readFile(String filename) throws FileNotFoundException{

        //read from pre committed local data
        FileTuple preCommittedFile = queuedFileSaves.get(filename);
        //todo: this assumes preCommittedFiles are always the most upto date.
        if(preCommittedFile != null) {
            return preCommittedFile.getData();
        }

        //get bundleID and cache related metadata
        String bundleID = fetchBundleID(filename);
        if(bundleID == null) {
            throw new FileNotFoundException("Could not find the metadata in any of the locations");
        }
        if(fileToBundleMapper.getBundleID(filename)==null) {
            //this means locally the meta data is new.
            //We should also get the bundleToFiles metadata
            HashSet<String> filesInBundle = fetchFilesInBundle(bundleID);
            if(filesInBundle == null) {
                throw new FileNotFoundException("Could not find the metadata in any of the locations");
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
        FileTuple committedFile = cachedBundles.get(bundleID).getFile(filename);

        return committedFile.getData();
    }
    private String fetchBundleID(String filename) {
        String fetchedBundleID = fileToBundleMapper.getBundleID(filename);

        Iterator<IMetadataStorage> it = metadataLocations.iterator();
        while(it.hasNext() && fetchedBundleID == null) {
            IMetadataStorage metadataLocation = it.next();
            try {
                fetchedBundleID = metadataLocation.readMetadata(filename);
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

        Iterator<IMetadataStorage> it = metadataLocations.iterator();
        while(it.hasNext() && fileInBundle == null) {
            IMetadataStorage metadataLocation = it.next();
            try {
                String fileInBundleJson = metadataLocation.readMetadata(bundleID);
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

        Iterator<IDataStorage> it = dataLocations.iterator();
        while(it.hasNext() && fetchedBundle == null) {
            IDataStorage dataLocation = it.next();
            try {
                byte[] bundleBytes = dataLocation.read(bundleID);
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

        HashSet<MetadataTuple> metadataWrites = new HashSet<>();
        HashSet<String> metadataDeletes = new HashSet<>();

        HashSet<FileTuple> dataWrites = new HashSet<>();
        HashSet<String> dataDeletes = new HashSet<>();

        HashSet<String> dirtyBundleMappings = fileToBundleMapper.getAndClearDirtyBundleMappings();

        for (String dirtyBundleMapping:dirtyBundleMappings) {
            try {
                String jsonList = JSON.SetOfStringsToJson(fileToBundleMapper.getFilesInBundle(dirtyBundleMapping));
                metadataWrites.add(new MetadataTuple(dirtyBundleMapping, jsonList));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
        }

        HashSet<String> dirtyFileMappings = fileToBundleMapper.getAndClearDirtyFileMappings();

        for (String dirtyFileMapping:dirtyFileMappings) {
            String bundleID = fileToBundleMapper.getBundleID(dirtyFileMapping);
            metadataWrites.add(new MetadataTuple(dirtyFileMapping, bundleID));
        }

        HashSet<String> deadBundleIDs = fileToBundleMapper.getAndClearDeadBundleIDs();
        cleanDeadCachedBundles(deadBundleIDs);

        metadataDeletes.addAll(deadBundleIDs);
        dataDeletes.addAll(deadBundleIDs);

        dataWrites.add(new FileTuple(newBundle.getBundleID(), Bundle.serializeBundle(newBundle)));

        Iterator<IMetadataStorage> itMetadata = metadataLocations.iterator();
        while(itMetadata.hasNext()) {
            IMetadataStorage metadataLocation = itMetadata.next();
            PendingMetadataActions pendingDataActions = metadataLocation.getPendingActions();
            pendingDataActions.queueWrites(metadataWrites);
            pendingDataActions.queueDeletes(metadataDeletes);
        }

        Iterator<IDataStorage> itData = dataLocations.iterator();
        while(itData.hasNext()) {
            IDataStorage dataLocation = itData.next();
            PendingDataActions pendingDataActions = dataLocation.getPendingActions();
            pendingDataActions.queueWrites(dataWrites);
            pendingDataActions.queueDeletes(dataDeletes);
        }
    }
    private boolean pushChangesToStorage() {
        boolean everythingFinished = true;

        Iterator<IMetadataStorage> itMetadata = metadataLocations.iterator();
        while(itMetadata.hasNext()) {
            IMetadataStorage metadataLocation = itMetadata.next();
            if(!pushMetadataWritesToRemote(metadataLocation)) {
                everythingFinished = false;
            }
            if(!pushMetadataDeletesToRemote(metadataLocation)) {
                everythingFinished = false;
            }
        }

        Iterator<IDataStorage> itData = dataLocations.iterator();
        while(itData.hasNext()) {
            IDataStorage dataLocation = itData.next();
            if(!pushDataWritesToRemote(dataLocation)) {
                everythingFinished = false;
            }
            if(!pushDataDeletesToRemote(dataLocation)) {
                everythingFinished = false;
            }
        }

        return everythingFinished;
    }
    private boolean pushDataWritesToRemote(IDataStorage dataLocation){
        PendingDataActions pendingDataActions = dataLocation.getPendingActions();
        //update file mapping
        FileTuple dirtyFile = pendingDataActions.popWrite();
        try {
            while (dirtyFile != null) {
                dataLocation.write(dirtyFile.getFileName(), dirtyFile.getData());
                dirtyFile = pendingDataActions.popWrite();
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            //replace the unsuccessful save back into the pending data structure
            pendingDataActions.queueWrite(dirtyFile);
            return false;
        }
        return true;
    }
    private boolean pushDataDeletesToRemote(IDataStorage dataLocation){
        PendingDataActions pendingDataActions = dataLocation.getPendingActions();
        //update file mapping
        String deadFile = pendingDataActions.popDelete();
        try {
            while (deadFile != null) {
                dataLocation.delete(deadFile);
                deadFile = pendingDataActions.popDelete();
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            //replace the unsuccessful save back into the pending data structure
            pendingDataActions.queueDelete(deadFile);
            return false;
        }
        return true;
    }
    private boolean pushMetadataWritesToRemote(IMetadataStorage metadataLocation){
        PendingMetadataActions pendingDataActions = metadataLocation.getPendingActions();
        //update file mapping
        MetadataTuple dirtyMetadata = pendingDataActions.popWrite();
        try {
            while (dirtyMetadata != null) {
                metadataLocation.writeMetadata(dirtyMetadata.getKey(), dirtyMetadata.getValue());
                dirtyMetadata = pendingDataActions.popWrite();
            }
        } catch (ConnectException e) {
            e.printStackTrace();
            //replace the unsuccessful save back into the pending data structure
            pendingDataActions.queueWrite(dirtyMetadata);
            return false;
        }
        return true;
    }
    private boolean pushMetadataDeletesToRemote(IMetadataStorage metadataLocation){
        PendingMetadataActions pendingDataActions = metadataLocation.getPendingActions();
        //update file mapping
        String deadMetadata = pendingDataActions.popDelete();
        try {
            while (deadMetadata != null) {
                metadataLocation.deleteMetadata(deadMetadata);
                deadMetadata = pendingDataActions.popDelete();
            }
        } catch (ConnectException e) {
            e.printStackTrace();

            //replace the unsuccessful save back into the pending data structure
            pendingDataActions.queueDelete(deadMetadata);
            return false;
        }
        return true;
    }
}
