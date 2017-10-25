package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import jnr.ffi.types.mode_t;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.util.List;

public class CejfsFuseFS extends FuseStubFS {

    //https://www.cs.hmc.edu/~geoff/classes/hmc.cs135.201109/homework/fuse/fuse_doc.html

    private IBundleClient bundleClient;
    private IMetadataClient metadataClient;

    public CejfsFuseFS(IBundleClient bundleClient, IMetadataClient metadataClient) {
        this.bundleClient = bundleClient;
        this.metadataClient = metadataClient;
    }

    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi) {
        return 0;
    }
}
