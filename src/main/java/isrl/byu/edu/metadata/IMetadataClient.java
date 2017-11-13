package isrl.byu.edu.metadata;

import java.util.List;
import java.util.Optional;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    Optional<MetadataHandle> getMetadata(String fullPath);

    /* Directory methods */
    boolean addChild(String fullPath, MetadataHandle handle);
    boolean removeChild(String fullPath);
    boolean renameFolder(String fullPath, String newName);

    List<String> listChildren(String parent);

    /* File methods */
    boolean updateFilesize(String fullPath, long fileSize);
    boolean renameFile(String fullPath, String newName);


}
