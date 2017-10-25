package isrl.byu.edu.metadata;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    default boolean doesMetadataExist(String path) {
        return this.getMetadata(path).isPresent();
    }

    Optional<MetadataHandle> createFile(String path);
    Optional<MetadataHandle> createDirectory(String path);

    boolean deleteMetadata(String path);

    Optional<MetadataHandle> getMetadata(String path);


}
