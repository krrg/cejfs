package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;

public abstract class FusePath {

    private String name;
    private DirectoryProxy parent;

    private IMetadataClient metadataClient;
    private IBundleClient bundleClient;
    private FuseContext fuseContext;

    FusePath(String name, ProxyParameters proxyParameters) {
        this(name, null, proxyParameters);
    }

    FusePath(String name, DirectoryProxy parent, ProxyParameters proxyParameters) {
        this.name = name;
        this.parent = parent;

        this.metadataClient = proxyParameters.getMetadataClient();
        this.bundleClient = proxyParameters.getBundleClient();
        this.fuseContext = proxyParameters.getContext();
    }

    synchronized void delete() {
        if (parent != null) {
            parent.deleteChild(this);
            parent = null;
        }
    }

    protected FusePath find(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(name) || path.isEmpty()) {
            return this;
        }
        return null;
    }

    protected abstract void getattr(FileStat stat);

    void rename(String newName) {
        while (newName.startsWith("/")) {
            newName = newName.substring(1);
        }
        name = newName;
    }

    protected FuseContext getContext() {
        return this.fuseContext;
    }

    protected IMetadataClient getMetadataClient() {
        return this.metadataClient;
    }

    protected IBundleClient getBundleClient() {
        return this.bundleClient;
    }

    protected DirectoryProxy getParent() {
        return this.parent;
    }

    protected String getName() {
        return this.name;
    }

    protected void setParent(DirectoryProxy path) {
        this.parent = path;
    }

    protected ProxyParameters getProxyParameters() {
        return new ProxyParameters(
                this.fuseContext, this.metadataClient, this.bundleClient
        );
    }


}
