package isrl.byu.edu;

import isrl.byu.edu.bundle.BundleClient;
import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import isrl.byu.edu.metadata.InMemoryMetadataClient;
import isrl.byu.edu.metadata.RedisMetadataClient;
import isrl.byu.edu.storage.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Paths;
import java.util.Random;

public class Main {

    static String redisURL = "redis://127.0.0.1:6379";

    public static void main(String[] args) {

        //HelloS3 S3 = new HelloS3();
        //try {
        //    S3.runTest();
        //} catch (IOException e) {
        //    e.printStackTrace();
        //}

        System.out.println("hello world");
        //try {
        //    runBundleTest1();
        //    runBundleTest2();
        //    runBundleTest3();
        //} catch (FileNotFoundException e) {
        //    e.printStackTrace();
        //}

        BundleClient bundleClient = new BundleClient();

        //bundleClient.addDataLocation(new AWSDataStorage());
        bundleClient.addDataLocation(new InMemoryDataStorage());

        bundleClient.addMetadataLocation(new RedisMetadataStorage(redisURL));
        //bundleClient.addMetadataLocation(new InMemoryMetadataStorage());

        IMetadataClient metadataClient = new RedisMetadataClient(redisURL);
        //IMetadataClient metadataClient = new InMemoryMetadataClient();

        CejfsFuseFS fs = new CejfsFuseFS(metadataClient, bundleClient);

        try {
            fs.mount(Paths.get("/tmp/cejfs2"), true, false);
        } finally {
            fs.umount(); // Could they not spell "unmount"
        }

    }

    private static void runBundleTest1() throws FileNotFoundException {
        System.out.println("TEST 1 - read files after the local cache is cleared ======");

        Random r = new Random();

        IDataStorage inMemoryDataStorage = new InMemoryDataStorage();
        IDataStorage awsDataStorage = new AWSDataStorage();

        IMetadataStorage inMemoryMetadataStorage = new InMemoryMetadataStorage();
        IMetadataStorage redisMetaDataStorage = new RedisMetadataStorage(redisURL);

        IBundleClient bundleClient = new BundleClient();
        bundleClient.addDataLocation(inMemoryDataStorage);
        //bundleClient.addDataLocation(awsDataStorage);
        //bundleClient.addMetadataLocation(inMemoryMetadataStorage);
        bundleClient.addMetadataLocation(redisMetaDataStorage);

        //create a new local cache, simulating a restart
        IBundleClient bundleClient2 = new BundleClient();
        bundleClient2.addDataLocation(inMemoryDataStorage);
        //bundleClient2.addDataLocation(awsDataStorage);
        //bundleClient2.addMetadataLocation(inMemoryMetadataStorage);
        bundleClient2.addMetadataLocation(redisMetaDataStorage);

        int FILES_TO_CREATE = 50;
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            saveFile(bundleClient, generateRandomBytes(r), String.valueOf(i));
        }

        // attempt to read files. See if it works
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            byte[] file1 = readFile(bundleClient, String.valueOf(i));
            byte[] file2 = readFile(bundleClient2, String.valueOf(i));
            boolean sameSize = byteSameSize(file1, file2);
            System.out.println("File " + i + " was is the same size: " + sameSize);
        }

        //force a flush, to save all files to remote servers
        bundleClient.flush();
        System.out.println("====== After a flush ======");

        // attempt to read files. See if it works
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            byte[] file1 = readFile(bundleClient, String.valueOf(i));
            byte[] file2 = readFile(bundleClient2, String.valueOf(i));
            boolean sameSize = byteSameSize(file1, file2);
            System.out.println("File " + i + " was is the same size: " + sameSize);
        }
    }

    private static void runBundleTest2() throws FileNotFoundException{
        System.out.println("TEST 2 - different local machines writing to remote client ======");

        Random r = new Random();

        IDataStorage inMemoryDataStorage = new InMemoryDataStorage();
        IDataStorage awsDataStorage = new AWSDataStorage();

        IMetadataStorage inMemoryMetadataStorage = new InMemoryMetadataStorage();
        IMetadataStorage redisMetaDataStorage = new RedisMetadataStorage(redisURL);

        IBundleClient bundleClient = new BundleClient();
        bundleClient.addDataLocation(inMemoryDataStorage);
        //bundleClient.addDataLocation(awsDataStorage);
        //bundleClient.addMetadataLocation(inMemoryMetadataStorage);
        bundleClient.addMetadataLocation(redisMetaDataStorage);

        //multiple users
        IBundleClient bundleClient2 = new BundleClient();
        bundleClient2.addDataLocation(inMemoryDataStorage);
        //bundleClient2.addDataLocation(awsDataStorage);
        //bundleClient2.addMetadataLocation(inMemoryMetadataStorage);
        bundleClient2.addMetadataLocation(redisMetaDataStorage);

        int FILES_TO_CREATE = 40;
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            saveFile(bundleClient, generateRandomBytes(r), String.valueOf(i));
        }

        for(int i = 40; i < FILES_TO_CREATE*2; i++) {
            saveFile(bundleClient2, generateRandomBytes(r), String.valueOf(i));
        }

        bundleClient.flush();
        bundleClient2.flush();

        // attempt to read files. See if it works
        for(int i = 0; i < FILES_TO_CREATE*2; i++) {
            byte[] file1 = readFile(bundleClient, String.valueOf(i));
            byte[] file2 = readFile(bundleClient2, String.valueOf(i));
            boolean sameSize = byteSameSize(file1, file2);
            System.out.println("File " + i + " was is the same size: " + sameSize);
        }

    }

    private static void runBundleTest3() throws FileNotFoundException{
        System.out.println("TEST 3 - resave files in order to force a deletion of an old bundle ======");

        Random r = new Random();

        IDataStorage inMemoryDataStorage = new InMemoryDataStorage();
        IDataStorage awsDataStorage = new AWSDataStorage();

        IMetadataStorage inMemoryMetadataStorage = new InMemoryMetadataStorage();
        IMetadataStorage redisMetaDataStorage = new RedisMetadataStorage(redisURL);

        IBundleClient bundleClient = new BundleClient();
        bundleClient.addDataLocation(inMemoryDataStorage);
        //bundleClient.addDataLocation(awsDataStorage);
        //bundleClient.addMetadataLocation(inMemoryMetadataStorage);
        bundleClient.addMetadataLocation(redisMetaDataStorage);

        int FILES_TO_CREATE = 100;
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            saveFile(bundleClient, generateRandomBytes(r), String.valueOf(i++));
        }

        //see if partial bundle changes update correctly
        for(int i = 0; i < 10; i++) {
            saveFile(bundleClient, generateRandomBytes(r), String.valueOf(i++));
        }
        bundleClient.flush();

        //save them again to rebundle
        for(int i = 0; i < FILES_TO_CREATE; i++) {
            saveFile(bundleClient, generateRandomBytes(r), String.valueOf(i++));
        }

        bundleClient.flush();
    }


    private static byte[] readFile(IBundleClient bundleClient, String filename) throws FileNotFoundException {
        byte[] fileBytes = null;
        fileBytes = bundleClient.readFile (filename);
        return fileBytes;
    }

    private static boolean saveFile(IBundleClient bundleClient, byte[] fileBytes, String filename) {
        return bundleClient.saveFile(fileBytes, filename);
    }

    private static boolean byteSameSize(byte[] file1, byte[] file2) {
        if(file1 == null && file2 == null) {
            return true;
        }
        if(file1 == null && file2 != null) {
            return false;
        }
        if(file1 != null && file2 == null) {
            return false;
        }
        if(file1.length == file2.length) {
            return true;
        }
        return false;
    }
    private static byte[] generateRandomBytes(Random r) {
        int MEGABYTE = 1000000;
        int size = (r.nextInt() & Integer.MAX_VALUE) % MEGABYTE;
        byte[] myBytes = new byte[size];
        r.nextBytes(myBytes);
        return myBytes;
    }
}
