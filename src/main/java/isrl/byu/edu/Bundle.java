package isrl.byu.edu;

import java.util.ArrayList;

public class Bundle {

    ArrayList<BundleFileData> files;
    String bundleID;

    public Bundle(String bundleId, ArrayList<BundleFileData> files)
    {
        this.bundleID = bundleId;
        this.files = files;
    }
}
