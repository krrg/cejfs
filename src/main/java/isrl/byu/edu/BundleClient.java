package isrl.byu.edu;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class BundleClient implements IBundleClient {

    FileToBundleMapper fileToBundleMapper = new FileToBundleMapper();
    HashMap<String, BundleFileData> queuedFileSaves = new HashMap<>();

    //todo clean up of old cached bundles
    HashMap<String, Bundle> cachedBundles = new HashMap<>();

    @Override
    public boolean saveFile(byte[] bytes, String filename) {

        queuedFileSaves.put(filename, bytes);
        return true;
    }

    @Override
    public byte[] readFile(String filename) {
        String bundleID = fileToBundleMapper.getBundleID(filename);
        if(!cachedBundles.containsKey(bundleID))
        {
            if(!fetchBundle(bundleID))
            {
                //todo: do something smart. There was no bundle from aws
                throw new RuntimeException();
            }
        }

        return extractFileFromBundle(filename, bundleID);
    }

    private boolean fetchBundle(String bundleId)
    {
        //todo: BIG TODO get bundle from aws
        //todo: create a better fake bundle
        boolean successfulPullFromAWS = true;
        byte[] myFakeBundle = new byte[10];
        if(myFakeBundle != null) {
            queuedFileSaves.put(bundleId, myFakeBundle);
        }
        else
        {
            successfulPullFromAWS = false;
        }
        return successfulPullFromAWS;
    }

    private byte[] extractFileFromBundle(String filename, String bundleId)
    {
        return new byte[0];
    }


    @Override
    public boolean flush() {

        Bundle newBundle = createBundle();
        fileToBundleMapper.remapBundle(newBundle);
        pushBundleToS3(newBundle);

        cleanDeadBundles();

        return false;
    }

    private boolean cleanDeadBundles()
    {
        //clean dead bundles
        String deadBundleId = fileToBundleMapper.popDeadBundleId();
        while(deadBundleId != null)
        {
            deleteBundle(deadBundleId);
            deadBundleId = fileToBundleMapper.popDeadBundleId();
        }
    }

    private boolean deleteBundle(String bundleId) {
        return false;
    }

    private Bundle createBundle()
    {
        String bundleId = generateBundleId();

        ArrayList<BundleFileData> filesForBundle = new ArrayList<>();
        for (BundleFileData bundleFileData: this.queuedFileSaves.values()) {
            filesForBundle.add(bundleFileData);
        }
        this.queuedFileSaves.clear();

        Bundle newBundle = new Bundle(bundleId, filesForBundle);
        return newBundle;
    }

    private String generateBundleId()
    {
        return "";
    }

    private boolean pushBundleToS3(Bundle newBundle)
    {
        return false;
    }
}
