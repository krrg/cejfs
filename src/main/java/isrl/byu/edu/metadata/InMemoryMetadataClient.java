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
        metadataHandlesMap.remove(fullPath);
        boolean successful = !getMetadata(fullPath).isPresent();
        return successful;
    }

    @Override
    public boolean renameFolder(String fullPath, String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }

        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        String filename = FilePathUtils.getFileName(fullPath);
        if(parentFullPath == null || filename == null)
        {
            return false;
        }

        HashSet<String> parentFileDirectory = fileDirectoryStructure.get(parentFullPath);
        HashSet<String> fileDirectory = fileDirectoryStructure.get(fullPath);

        if(parentFileDirectory == null || fileDirectory == null)
        {
            return false;
        }

        boolean exists = parentFileDirectory.contains(filename) && this.metadataHandlesMap.containsKey(fullPath);
        if(!exists){
            return false;
        }

        parentFileDirectory.remove(filename);
        parentFileDirectory.add(newName);

        MetadataHandle metadataHandle = this.metadataHandlesMap.remove(fullPath);
        metadataHandlesMap.put(FilePathUtils.getFullPath(parentFullPath,newName),metadataHandle);

        fileDirectory = fileDirectoryStructure.remove(fullPath);
        fileDirectoryStructure.put(FilePathUtils.getFullPath(parentFullPath,newName), fileDirectory);

        return renameChildrenPaths(fullPath, FilePathUtils.getFullPath(parentFullPath,newName));
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
    public List<String> listChildren(String parent) {
        HashSet<String> fileDirectory = fileDirectoryStructure.get(parent);
        if(fileDirectory == null)
        {
            return new ArrayList<>();
        }
        String[] childrenArray = (String[])fileDirectory.toArray();
        return new ArrayList<>(Arrays.asList(childrenArray));
    }

    @Override
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
    public boolean renameFile(String fullPath, String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }

        String parentFullPath = FilePathUtils.getParentFullPath(fullPath);
        String filename = FilePathUtils.getFileName(fullPath);
        if(parentFullPath == null || filename == null)
        {
            return false;
        }

        HashSet<String> fileDirectory = fileDirectoryStructure.get(parentFullPath);
        if(fileDirectory == null)
        {
            return false;
        }

        boolean exists = fileDirectory.contains(filename) && this.metadataHandlesMap.containsKey(fullPath);
        if(!exists){
            return false;
        }

        fileDirectory.remove(filename);
        fileDirectory.add(newName);

        MetadataHandle metadataHandle = this.metadataHandlesMap.remove(fullPath);
        metadataHandlesMap.put(FilePathUtils.getFullPath(parentFullPath,newName),metadataHandle);
        return true;
    }

}
