package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import isrl.byu.edu.metadata.MetadataHandle;
import jnr.ffi.types.mode_t;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.util.List;
import java.util.Optional;

public class CejfsFuseFS extends FuseStubFS {

    //https://www.cs.hmc.edu/~geoff/classes/hmc.cs135.201109/homework/fuse/fuse_doc.html

    private IBundleClient bundleClient;
    private IMetadataClient metadataClient;

    public CejfsFuseFS(IBundleClient bundleClient, IMetadataClient metadataClient) {
        this.bundleClient = bundleClient;
        this.metadataClient = metadataClient;
    }

    @Override
    public int getattr(String path, FileStat stat) {
        Optional<MetadataHandle> optMetadata = metadataClient.getMetadata(path);

        if (! optMetadata.isPresent()) {
            return -1; /* I think this means something bad happened */
        }

        MetadataHandle metadata = optMetadata.get();


        return 0;
    }
}
