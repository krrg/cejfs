package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.FuseFillDir;
import ru.serce.jnrfuse.struct.FileStat;

import java.util.ArrayList;
import java.util.List;

public class DirectoryProxy extends FusePath {

    List<FusePath> contents = new ArrayList<>();

    DirectoryProxy(String name, ProxyParameters proxyParameters) {
        super(name, proxyParameters);
    }

    DirectoryProxy(String name, DirectoryProxy parent, ProxyParameters proxyParameters) {
        super(name, parent, proxyParameters);
    }

    public synchronized void add(FusePath p) {
        contents.add(p);
        p.setParent(this);
    }

    protected synchronized void deleteChild(FusePath child) {
        contents.remove(child);
    }

    @Override
    protected FusePath find(String path) {
        if (super.find(path) != null) {
            return super.find(path);
        }
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        synchronized (this) {
            if (!path.contains("/")) {
                for (FusePath p : contents) {
                    if (p.getName().equals(path)) {
                        return p;
                    }
                }
                return null;
            }
            String nextName = path.substring(0, path.indexOf("/"));
            String rest = path.substring(path.indexOf("/"));
            for (FusePath p : contents) {
                if (p.getName().equals(nextName)) {
                    return p.find(rest);
                }
            }
        }
        return null;
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFDIR | 0777);
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().pid.get());
    }

    synchronized void mkdir(String lastComponent) {
        contents.add(new DirectoryProxy(lastComponent, this, this.getProxyParameters()));
    }

    synchronized void mkfile(String lastComponent) {
        contents.add(new FileProxy(lastComponent, this, this.getProxyParameters()));
    }

    public synchronized void read(Pointer buf, FuseFillDir filler) {
        for (FusePath p : contents) {
            filler.apply(buf, p.getName(), null, 0);
        }
    }
}
