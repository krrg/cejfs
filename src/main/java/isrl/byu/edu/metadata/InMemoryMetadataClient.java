package isrl.byu.edu.metadata;

import isrl.byu.edu.utils.FilePathUtils;

import java.util.*;

public class InMemoryMetadataClient implements IMetadataClient{

    private HashMap<String, HashSet<String>> fileDirectoryStructure = new HashMap<>();
    private HashMap<String, MetadataHandle> metadataHandlesMap = new HashMap<>();

    @Override
    public Optional<MetadataHandle> getMetadata(String fullPath) {
        return Optional.ofNullable(metadataHandlesMap.get(fullPath));
    }

    @Override
    public boolean addChild(String fullPath, MetadataHandle handle) {
        if(fullPath == null || handle == null) {
            return false;
        }
        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        if(parentFullPath == null)
        {
            return false;
        }

        HashSet<String> fileDirectory = fileDirectoryStructure.get(parentFullPath);
        if(fileDirectory == null)
        {
            fileDirectoryStructure.put(parentFullPath, new HashSet());
            fileDirectory = fileDirectoryStructure.get(parentFullPath);
        }

        String fileName = FilePathUtils.getFileName(fullPath);

        if(fileName == null)
        {
            return false;
        }

        boolean alreadyExists = !fileDirectory.add(fileName);

        if(alreadyExists){
            return false;
        }

        metadataHandlesMap.put(fullPath,handle);
        boolean successful = getMetadata(fullPath).isPresent();
        return successful;
    }

    @Override
    public boolean removeChild(String fullPath) {

        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        String filename = FilePathUtils.getFileName(fullPath);
        if(parentFullPath == null || filename == null)
        {
            return false;
        }

        HashSet<String> parentFileDirectory = fileDirectoryStructure.get(parentFullPath);

        if(parentFileDirectory == null)
        {
            return false;
        }

        boolean exists = parentFileDirectory.contains(filename) && this.metadataHandlesMap.containsKey(fullPath);
        if(!exists){
            return false;
        }

        HashSet<String> fileDirectory = fileDirectoryStructure.remove(fullPath);
        if(fileDirectory != null)
        {
            //recurse through children
            for (String child:
            fileDirectory) {
                removeChild(FilePathUtils.getFullPath(fullPath,child));
            }
        }

        parentFileDirectory.remove(filename);
        this.metadataHandlesMap.remove(fullPath);

        boolean successful = !getMetadata(fullPath).isPresent();

        return successful;
    }

    @Override
    public boolean renameFolder(String fullPath, String newFilePath) {

        String newParentFullPath = FilePathUtils.getParentFullPath(newFilePath);
        String newFilename = FilePathUtils.getFileName(newFilePath);

        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        String filename = FilePathUtils.getFileName(fullPath);
        if(newParentFullPath == null || newFilename == null || parentFullPath == null || filename == null)
        {
            return false;
        }

        HashSet<String> parentFileDirectory = fileDirectoryStructure.get(parentFullPath);
        HashSet<String> newParentFileDirectory = fileDirectoryStructure.get(newParentFullPath);

        if(parentFileDirectory == null)
        {
            return false;
        }

        if(newParentFileDirectory == null)
        {
            fileDirectoryStructure.put(newParentFullPath, new HashSet());
            newParentFileDirectory = fileDirectoryStructure.get(newParentFullPath);
        }

        boolean exists = parentFileDirectory.contains(filename) && this.metadataHandlesMap.containsKey(fullPath);
        if(!exists){
            return false;
        }

        parentFileDirectory.remove(filename);
        newParentFileDirectory.add(newFilename);

        MetadataHandle metadataHandle = this.metadataHandlesMap.remove(fullPath);
        metadataHandlesMap.put(FilePathUtils.getFullPath(newParentFullPath,newFilename),metadataHandle);

        boolean successful = !getMetadata(fullPath).isPresent();


        HashSet<String> fileDirectory = fileDirectoryStructure.get(fullPath);

        if(fileDirectory != null)
        {
            fileDirectory = fileDirectoryStructure.remove(fullPath);
            fileDirectoryStructure.put(FilePathUtils.getFullPath(newParentFullPath,newFilename), fileDirectory);
            successful &= renameChildrenPaths(fullPath, FilePathUtils.getFullPath(newParentFullPath,newFilename));
        }

        return successful;
    }

    private boolean renameChildrenPaths(String oldFullPath, String newFullPath)
    {
        //need to replace keys used in metadataHandlesMap and fileDirectoryStructure
        boolean fullySuccessful = true;

        HashSet<String> fileDirectory = fileDirectoryStructure.get(newFullPath);
        for (String fileOrFolderName:
        fileDirectory) {
            String oldChildPath = FilePathUtils.getFullPath(oldFullPath,fileOrFolderName);
            String newChildPath = FilePathUtils.getFullPath(newFullPath,fileOrFolderName);

            MetadataHandle metadataHandle = metadataHandlesMap.remove(oldChildPath);
            metadataHandlesMap.put(newChildPath,metadataHandle);
        }

        for (String fileOrFolderName:
                fileDirectory) {
            String oldChildPath = FilePathUtils.getFullPath(oldFullPath,fileOrFolderName);
            String newChildPath = FilePathUtils.getFullPath(newFullPath,fileOrFolderName);

            MetadataHandle metadataHandle = metadataHandlesMap.get(newChildPath);
            if(metadataHandle.isDirectory())
            {
                HashSet<String> childFileDirectory = fileDirectoryStructure.remove(oldChildPath);
                fileDirectoryStructure.put(newChildPath, childFileDirectory);
                fullySuccessful = renameChildrenPaths(oldChildPath, newChildPath);
            }

        }
        return fullySuccessful;
    }


    @Override
    public Collection<String> listChildren(String parent) {
        HashSet<String> fileDirectory = fileDirectoryStructure.get(parent);
        if(fileDirectory == null)
        {
            return new ArrayList<>();
        }
        return fileDirectory;
    }

    public boolean updateFilesize(String fullPath, long fileSize) {
        if(fullPath == null) {
            return false;
        }

        Optional<MetadataHandle> metadataHandleOptional = getMetadata(fullPath);
        boolean exists = metadataHandleOptional.isPresent();
        if(exists)
        {
            MetadataHandle metadataHandle = metadataHandleOptional.get();
            metadataHandle.setSize(fileSize);
        }
        return exists;
    }

    @Override
    public boolean renameFile(String fullPath, String newFilePath) {
        String newParentFullPath = FilePathUtils.getParentFullPath(newFilePath);
        String newFilename = FilePathUtils.getFileName(newFilePath);

        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        String filename = FilePathUtils.getFileName(fullPath);
        if(newParentFullPath == null || newFilename == null || parentFullPath == null || filename == null)
        {
            return false;
        }

        HashSet<String> parentFileDirectory = fileDirectoryStructure.get(parentFullPath);
        HashSet<String> newParentFileDirectory = fileDirectoryStructure.get(newParentFullPath);

        if(parentFileDirectory == null)
        {
            return false;
        }

        if(newParentFileDirectory == null)
        {
            fileDirectoryStructure.put(newParentFullPath, new HashSet());
            newParentFileDirectory = fileDirectoryStructure.get(newParentFullPath);
        }

        boolean exists = parentFileDirectory.contains(filename) && this.metadataHandlesMap.containsKey(fullPath);
        if(!exists){
            return false;
        }

        parentFileDirectory.remove(filename);
        newParentFileDirectory.add(newFilename);

        MetadataHandle metadataHandle = this.metadataHandlesMap.remove(fullPath);
        metadataHandlesMap.put(FilePathUtils.getFullPath(newParentFullPath,newFilename),metadataHandle);

        boolean successful = !getMetadata(fullPath).isPresent();
        return successful;
    }

}
