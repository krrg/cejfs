package isrl.byu.edu.bundle;

public interface IBundleClient {

    boolean saveFile(byte[] bytes, String filename);
    byte[] readFile(String filename);

    boolean flush();
}
