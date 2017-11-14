package isrl.byu.edu;

import isrl.byu.edu.metadata.MetadataHandle;
import isrl.byu.edu.utils.FilePathUtils;
import jnr.ffi.Pointer;
import jnr.ffi.Runtime;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

import java.util.Collection;
import java.util.List;

public class DirectoryProxy extends FusePath {

    DirectoryProxy(String fullPath, ProxyParameters proxyParameters) {
        super(fullPath, proxyParameters);
    }

    DirectoryProxy(String name, String parentFullPath, ProxyParameters proxyParameters) {
        super(FilePathUtils.getFullPath(parentFullPath, name), proxyParameters);
    }

    private synchronized void add(FusePath p) {
        FileStat fileStat = new FileStat(Runtime.getSystemRuntime());
        p.getattr(fileStat);
        MetadataHandle metadataHandle = new MetadataHandle(fileStat);

        this.getMetadataClient().addChild(p.getFullPath(),metadataHandle);
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFDIR | 0777);
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().pid.get());
    }

    @Override
    protected void rename(String newName) {
        this.getMetadataClient().renameFolder(getFullPath(), newName);
    }

    synchronized void mkdir(String lastComponent) {
        this.add(new DirectoryProxy(lastComponent, this.getFullPath(), this.getProxyParameters()));
    }

    synchronized void mkfile(String lastComponent) {
        this.add(new FileProxy(lastComponent, this.getFullPath(), this.getProxyParameters()));
    }

    public synchronized void read(Pointer buf, FuseFillDir filler) {
        Collection<String> children = this.getMetadataClient().listChildren(this.getFullPath());
        for (String fileName : children) {
            filler.apply(buf, fileName, null, 0);
        }
    }
}
