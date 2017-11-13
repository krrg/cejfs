package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import isrl.byu.edu.metadata.MetadataHandle;
import isrl.byu.edu.utils.FilePathUtils;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;

import java.util.Optional;

public abstract class FusePath {

    private boolean isDeleted = false;

    private String name;
    private String parentFullPath;

    private IMetadataClient metadataClient;
    private IBundleClient bundleClient;
    private FuseContext fuseContext;

    FusePath(String fullPath, ProxyParameters proxyParameters) {
        this.name = FilePathUtils.getFileName(fullPath);
        this.parentFullPath = FilePathUtils.getParentFullPath(fullPath);

        this.metadataClient = proxyParameters.getMetadataClient();
        this.bundleClient = proxyParameters.getBundleClient();
        this.fuseContext = proxyParameters.getContext();
    }

    synchronized void delete() {
        this.getMetadataClient().removeChild(this.getFullPath());
        isDeleted = true;
    }

    protected FusePath find(String path) {
        while (path.startsWith("/")) {
            path = path.substring(1);
        }
        if (path.equals(getFullPath()) || path.isEmpty()) {
            if(isDeleted){
                return null;
            }
            return this;
        }

        Optional<MetadataHandle> metadataHandleOptional = this.getMetadataClient().getMetadata(path);
        if(!metadataHandleOptional.isPresent())
        {
            return null;
        }

        MetadataHandle metadataHandle = metadataHandleOptional.get();
        if(metadataHandle.isDirectory())//is directory
        {
            return new DirectoryProxy(path,this.getProxyParameters());
        }
        else //is file
        {
            return new FileProxy(path,this.getProxyParameters());
        }
    }

    protected abstract void getattr(FileStat stat);
    protected abstract void rename(String newName);

    protected FuseContext getContext() {
        return this.fuseContext;
    }

    protected IMetadataClient getMetadataClient() {
        return this.metadataClient;
    }

    protected IBundleClient getBundleClient() {
        return this.bundleClient;
    }

    protected String getParentFullPath() {
        return this.parentFullPath;
    }

    protected String getName() {
        return this.name;
    }

    protected String getFullPath() {
        return FilePathUtils.getFullPath(this.parentFullPath, this.name);
    }

    protected void setParent(String parentFullPath) {
        this.parentFullPath = parentFullPath;
    }

    protected ProxyParameters getProxyParameters() {
        return new ProxyParameters(
                this.fuseContext, this.metadataClient, this.bundleClient
        );
    }


}
