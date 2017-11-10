package isrl.byu.edu.metadata;

import java.util.List;
import java.util.Optional;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    Optional<MetadataHandle> getMetadata(String fullPath);

    /* Directory methods */
    boolean add(String parentFullPath, String childName,  MetadataHandle handle);
    boolean remove(String parentFullPath, String childName);
    List<MetadataHandle> listChildren(String parent);

    /* File methods */


}
