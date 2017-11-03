package isrl.byu.edu.storage;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.Bucket;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.util.IOUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.UUID;

public class AWSDataStorage implements IDataStorage{

    private PendingDataActions pendingDataActions = new PendingDataActions();

    AmazonS3 s3;
    Region usWest2;
    final String BUCKET_NAME_ID = "my-bundle-storage";
    String bucketName;
    public AWSDataStorage()
    {
        s3 = new AmazonS3Client();
        usWest2 = Region.getRegion(Regions.US_WEST_2);
        s3.setRegion(usWest2);
        bucketName = null;

        try{
            for (Bucket bucket : s3.listBuckets()) {
                if (bucket.getName().contains(BUCKET_NAME_ID)) {
                    bucketName = bucket.getName();
                }
            }
            if (bucketName == null) {
                bucketName = BUCKET_NAME_ID + UUID.randomUUID();
                s3.createBucket(bucketName);
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
            throw new RuntimeException("AWS failed");
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
            throw new RuntimeException("AWS failed");
        }
    }


    @Override
    public String getID() {
        return "awsData";
    }

    @Override
    public PendingDataActions getPendingActions() {
        return pendingDataActions;
    }

    @Override
    public int write(String filename, byte[] data) throws ConnectException {

        int bytesWritten = 0;
        try{
            File file = createFile(filename, data);
            s3.putObject(new PutObjectRequest(bucketName, filename, file));
            bytesWritten = data.length;
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytesWritten;
    }

    @Override
    public byte[] read(String filename) throws FileNotFoundException, NoSuchFileException, ConnectException {
        byte[] data = null;
        try{

            S3Object object = s3.getObject(new GetObjectRequest(bucketName, filename));
            data = IOUtils.toByteArray(object.getObjectContent());
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return data;
    }

    @Override
    public boolean delete(String filename) throws ConnectException {
        boolean exists = false;
        try{
            exists = s3.doesObjectExist(bucketName, filename);
            if(exists)
            {
                s3.deleteObject(bucketName, filename);
            }
        } catch (AmazonServiceException ase) {
            System.out.println("Caught an AmazonServiceException, which means your request made it "
                    + "to Amazon S3, but was rejected with an error response for some reason.");
            System.out.println("Error Message:    " + ase.getMessage());
            System.out.println("HTTP Status Code: " + ase.getStatusCode());
            System.out.println("AWS Error Code:   " + ase.getErrorCode());
            System.out.println("Error Type:       " + ase.getErrorType());
            System.out.println("Request ID:       " + ase.getRequestId());
        } catch (AmazonClientException ace) {
            System.out.println("Caught an AmazonClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with S3, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message: " + ace.getMessage());
        }
        return exists;
    }


    private File createFile(String filename, byte[] data) throws IOException {
        File file = File.createTempFile(filename, ".bundle");
        file.deleteOnExit();

        FileOutputStream fos = new FileOutputStream(file);
        fos.write(data);
        fos.close();

        return file;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        AWSDataStorage otherStorage = (AWSDataStorage) other;
        if (this.getID() != otherStorage.getID()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getID().hashCode();
    }

}
