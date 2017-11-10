package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import jnr.ffi.Platform;
import jnr.ffi.Pointer;
import jnr.ffi.types.mode_t;
import jnr.ffi.types.off_t;
import jnr.ffi.types.size_t;
import ru.serce.jnrfuse.ErrorCodes;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.FuseStubFS;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseFileInfo;
import ru.serce.jnrfuse.struct.Statvfs;

import static jnr.ffi.Platform.OS.WINDOWS;

public class CejfsFuseFS extends FuseStubFS {

    private DirectoryProxy rootDirectory;
    private IMetadataClient metadataClient;
    private IBundleClient bundleClient;

    public CejfsFuseFS(IMetadataClient metadataClient, IBundleClient bundleClient) {

        this.metadataClient = metadataClient;
        this.bundleClient = bundleClient;
        rootDirectory = new DirectoryProxy("", new ProxyParameters(this.getContext(), this.metadataClient, this.bundleClient));

//        // Sprinkle some files around
//        rootDirectory.add(new FileProxy("Sample file.txt", "Hello there, feel free to look around.\n"));
//        rootDirectory.add(new DirectoryProxy("Sample directory"));
//        DirectoryProxy dirWithFiles = new DirectoryProxy("Directory with files");
//        rootDirectory.add(dirWithFiles);
//        dirWithFiles.add(new FileProxy("hello.txt", "This is some sample text.\n"));
//        dirWithFiles.add(new FileProxy("hello again.txt", "This another file with text in it! Oh my!\n"));
//        DirectoryProxy nestedDirectory = new DirectoryProxy("Sample nested directory");
//        dirWithFiles.add(nestedDirectory);
//        nestedDirectory.add(new FileProxy("So deep.txt", "Man, I'm like, so deep in this here file structure.\n"));
    }

    @Override
    public int create(String path, @mode_t long mode, FuseFileInfo fi) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        FusePath parent = getParentPath(path);
        if (parent instanceof DirectoryProxy) {
            ((DirectoryProxy) parent).mkfile(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }


    @Override
    public int getattr(String path, FileStat stat) {
        FusePath p = getPath(path);
        if (p != null) {
            p.getattr(stat);
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }

    private String getLastComponent(String path) {
        while (path.substring(path.length() - 1).equals("/")) {
            path = path.substring(0, path.length() - 1);
        }
        if (path.isEmpty()) {
            return "";
        }
        return path.substring(path.lastIndexOf("/") + 1);
    }

    private FusePath getParentPath(String path) {
        return rootDirectory.find(path.substring(0, path.lastIndexOf("/")));
    }

    private FusePath getPath(String path) {
        return rootDirectory.find(path);
    }


    @Override
    public int mkdir(String path, @mode_t long mode) {
        if (getPath(path) != null) {
            return -ErrorCodes.EEXIST();
        }
        FusePath parent = getParentPath(path);
        if (parent instanceof DirectoryProxy) {
            ((DirectoryProxy) parent).mkdir(getLastComponent(path));
            return 0;
        }
        return -ErrorCodes.ENOENT();
    }


    @Override
    public int read(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof FileProxy)) {
            return -ErrorCodes.EISDIR();
        }
        return ((FileProxy) p).read(buf, size, offset);
    }

    @Override
    public int readdir(String path, Pointer buf, FuseFillDir filter, @off_t long offset, FuseFileInfo fi) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof DirectoryProxy)) {
            return -ErrorCodes.ENOTDIR();
        }
        filter.apply(buf, ".", null, 0);
        filter.apply(buf, "..", null, 0);
        ((DirectoryProxy) p).read(buf, filter);
        return 0;
    }


    @Override
    public int statfs(String path, Statvfs stbuf) {
        if (Platform.getNativePlatform().getOS() == WINDOWS) {
            throw new UnsupportedOperationException("statfs Not supported");
        }
        return super.statfs(path, stbuf);
    }

    @Override
    public int rename(String path, String newName) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        FusePath newParent = getParentPath(newName);
        if (newParent == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(newParent instanceof DirectoryProxy)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        p.rename(newName.substring(newName.lastIndexOf("/")));
        ((DirectoryProxy) newParent).add(p);
        return 0;
    }

    @Override
    public int rmdir(String path) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof DirectoryProxy)) {
            return -ErrorCodes.ENOTDIR();
        }
        p.delete();
        return 0;
    }

    @Override
    public int truncate(String path, long offset) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof FileProxy)) {
            return -ErrorCodes.EISDIR();
        }
        ((FileProxy) p).truncate(offset);
        return 0;
    }

    @Override
    public int unlink(String path) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        p.delete();
        return 0;
    }

    @Override
    public int open(String path, FuseFileInfo fi) {
        return 0;
    }

    @Override
    public int write(String path, Pointer buf, @size_t long size, @off_t long offset, FuseFileInfo fi) {
        FusePath p = getPath(path);
        if (p == null) {
            return -ErrorCodes.ENOENT();
        }
        if (!(p instanceof FileProxy)) {
            return -ErrorCodes.EISDIR();
        }
        return ((FileProxy) p).write(buf, size, offset);
    }
}