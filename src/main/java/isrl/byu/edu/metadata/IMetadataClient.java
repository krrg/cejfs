package isrl.byu.edu.metadata;

import java.util.List;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    /* Directory methods */
    boolean add(String parent, String child, MetadataHandle handle);
    boolean remove(String parent, String child);
    List<MetadataHandle> listChildren(String parent);


}
