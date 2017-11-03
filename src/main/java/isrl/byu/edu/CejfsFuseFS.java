package isrl.byu.edu;

import isrl.byu.edu.bundle.Bundle;
import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import isrl.byu.edu.metadata.MetadataHandle;
import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
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

        /* Mutation is evidently how we return things */
        metadata.mutateFileStat(stat);
        return 0;
    }

    @Override
    public int mkdir(String path, long mode) {
        metadataClient.createDirectory(path);
        return 0;
    }

    @Override
    public int rmdir(String path) {
        System.out.println("Warning, not currently able to delete directories.");
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        /*
        Open a file. If you aren't using file handles, this function should just check for existence
        and permissions and return either success or an error code. If you use file handles,
        you should also allocate any necessary structures and set fi->fh. In addition, fi has
        some other fields that an advanced filesystem might find useful; see the structure definition
        in fuse_common.h for very brief commentary.
        */

        if (metadataClient.doesMetadataExist(path)) {
            return 0;
        } else {
            return -1;
        }
    }

    @Override
    public int read(String path, Pointer buf, long size, long offset, FuseFileInfo fi) {
        /*
        Read size bytes from the given file into the buffer buf, beginning offset bytes into the file.
        See read(2) for full details. Returns the number of bytes transferred,
        or 0 if offset was at or beyond the end of the file.
        Required for any sensible filesystem. super.read(path, buf, size, offset, fi);
        */
        try {
            byte[] fileBytes = bundleClient.readFile(path);

            if (offset + size > fileBytes.length) {
                System.err.println("Tried to read file of too much offset.");
                return -1;
            }

        } catch (ConnectException | FileNotFoundException | NoSuchFileException e) {
            e.printStackTrace();
            return -1;
        }
    }
}
