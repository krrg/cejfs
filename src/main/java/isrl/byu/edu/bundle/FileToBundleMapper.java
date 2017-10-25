package isrl.byu.edu.bundle;

import java.util.LinkedList;
import java.util.Queue;

public class FileToBundleMapper {

    private Queue<String> bundlesToBeDeleted = new LinkedList<String>();

    public String popDeadBundleId()
    {
        return bundlesToBeDeleted.poll();
    }

    public boolean remapBundle(Bundle newBundle)
    {

        return false;
    }

    private boolean remapFile(String filename)
    {
        //get oldBundleID from file-> bundle
        //update file -> bundle metadeta
        //update oldBundle metadata
            //if oldBundle metadata is empty, queue bundle for deletion

        //update newBundle metadata


        return false;
    }

    public String getBundleID(String filename)
    {
        return "";
    }

    public boolean flush()
    {
        return false;
    }

}
