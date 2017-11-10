package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import ru.serce.jnrfuse.struct.FuseContext;

public class ProxyParameters {

    private FuseContext context;
    private IMetadataClient metadataClient;
    private IBundleClient bundleClient;

    public ProxyParameters(FuseContext context, IMetadataClient metadataClient, IBundleClient bundleClient) {
        this.context = context;
        this.metadataClient = metadataClient;
        this.bundleClient = bundleClient;
    }


    public FuseContext getContext() {
        return context;
    }

    public IMetadataClient getMetadataClient() {
        return metadataClient;
    }

    public IBundleClient getBundleClient() {
        return bundleClient;
    }
}
