package isrl.byu.edu.metadata;

import java.nio.file.Path;
import java.util.Optional;

/**
 * Created by krr428 on 10/21/17.
 */
public interface IMetadataClient {

    default boolean doesMetadataExist(Path path) {
        return this.getMetadata(path).isPresent();
    }

    Optional<MetadataHandle> createFile(Path path);
    Optional<MetadataHandle> createDirectory(Path path);

    boolean deleteMetadata(Path path);

    Optional<MetadataHandle> getMetadata(Path path);


}
