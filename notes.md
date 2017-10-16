IMetadataClient

createFolderMetadata(path)
createFileMetadata(path)
MetadataObj readMetadata(path)
    MetadataObj.isFolder
    MetadataObj.isFile
deleteFolderMetadata(path)
deleteFileMetadata(path)
updateFolderMetadata(path)
updateFileMetadata(path)
flush()

IBundleClient

saveFile(bytes, filename)
flush()
readFile(filename)
private deleteBundle(id)

BundleToFileMapper
    File->BundleID
    Bundle-> List of Files
    