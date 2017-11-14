package isrl.byu.edu.metadata;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    Optional<MetadataHandle> getMetadata(String fullPath);

    boolean addChild(String fullPath, MetadataHandle handle);
    boolean removeChild(String fullPath);

    /* Directory methods */
    boolean renameFolder(String fullPath, String newName);

    Collection<String> listChildren(String parent);

    /* File methods */
    boolean updateFilesize(String fullPath, long fileSize);
    boolean renameFile(String fullPath, String newName);


}
