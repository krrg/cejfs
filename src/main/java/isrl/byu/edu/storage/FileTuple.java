package isrl.byu.edu.storage;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

public class FileTuple implements Serializable {
    private String filename;
    private byte[] data;
    private long fileSize;

    public String getFileName() { return filename; }
    public byte[] getData()
    {
        return data;
    }
    public long getFileSize()
    {
        return fileSize;
    }

    public FileTuple(String filename, byte[] data) {
        this.filename = filename;
        this.data = data;
        this.fileSize = data.length;
    }

    public boolean insertData(byte[] newData, int offset){

        if(offset + newData.length > data.length) {
            //need to extend array
            byte[] biggerData = new byte[data.length*2];
            System.arraycopy(data,0,biggerData,0,data.length);
            data = biggerData;
        }


        if(offset + newData.length > getFileSize()) {
            long fileSizeChange = newData.length - (getFileSize() - offset);
            this.fileSize += fileSizeChange;
        }
        //else {
            //do not need to extend array

        //   byte[] messyAdd = this.data.toByteArray();
        //    System.arraycopy(newData,0,messyAdd,offset,newData.length);
        //    this.data = new ByteArrayOutputStream();
        //    this.data.write(messyAdd,0,messyAdd.length);
        //}
        System.arraycopy(newData,0,data,offset,newData.length);

        return true;
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
        FileTuple otherFileTuple = (FileTuple) other;
        if (this.getFileName() != otherFileTuple.getFileName()) {
            return false;
        }

        if (this.getFileSize() != otherFileTuple.getFileSize())
        {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getFileName().hashCode();
    }

}
