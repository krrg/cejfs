package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import isrl.byu.edu.utils.FilePathUtils;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;

import java.io.FileNotFoundException;

public class FileProxy extends FusePath {

    FileProxy(String fullPath, ProxyParameters proxyParameters) {
        super(fullPath, proxyParameters);
    }

    FileProxy(String name, String parentFullPath, ProxyParameters proxyParameters) {
        super(FilePathUtils.getFullPath(parentFullPath, name), proxyParameters);
    }

    @Override
    protected void getattr(FileStat stat) {
//        getMetadataClient().getMetadata(this.getFullName())
//            .orElseThrow(RuntimeException::new)
//            .mutateFileStat(stat);

        stat.st_mode.set(FileStat.S_IFREG | 0777);
        long fileSize = 0;
        try {
            fileSize = getBundleClient().readFile(this.getFullPath()).length;
        } catch (FileNotFoundException e) {
            fileSize = 0;
        }
        stat.st_size.set(fileSize);
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().pid.get());
    }

    int read(Pointer buffer, long size, long offset)
    {
        byte [] fullFile = new byte[0];

        int actualSize=0;
        try {
            fullFile = getBundleClient().readFile(this.getFullPath());
            actualSize = (int)Math.min(fullFile.length, size);
            buffer.put(offset, fullFile, (int)offset, actualSize);
        } catch (FileNotFoundException e) {
            actualSize=0;
            e.printStackTrace();
        }

        return actualSize;

        //
//        System.out.println("I am reading from file named: `" + this.getFullName() + "`");
//
//        int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
//        byte[] bytesRead = new byte[bytesToRead];
//        synchronized (this) {
//            contents.position((int) offset);
//            contents.get(bytesRead, 0, bytesToRead);
//            buffer.put(0, bytesRead, 0, bytesToRead);
//            contents.position(0); // Rewind
//        }
//        return bytesToRead;
    }

    synchronized void truncate(long size) {
        throw new UnsupportedOperationException("Not supported by Runtime");


//        if (size < contents.capacity()) {
//            // Need to create a new, smaller buffer
//            ByteBuffer newContents = ByteBuffer.allocate((int) size);
//            byte[] bytesRead = new byte[(int) size];
//            contents.get(bytesRead);
//            newContents.put(bytesRead);
//            contents = newContents;
//        }
    }

    int write(Pointer buffer, long bufSize, long writeOffset) {
        if (writeOffset > 0) {
            System.out.println("Write offset was: " + writeOffset);
            throw new UnsupportedOperationException("Write offset cannot be non-zero");
        }

        byte [] allocated = new byte[(int) bufSize];
        buffer.get(0, allocated, 0, (int) bufSize);
        getBundleClient().saveFile(allocated, this.getFullPath());

//        int maxWriteIndex = (int) (writeOffset + bufSize);
//        byte[] bytesToWrite = new byte[(int) bufSize];
//        synchronized (this) {
//            if (maxWriteIndex > contents.capacity()) {
//                // Need to create a new, larger buffer
//                ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
//                newContents.put(contents);
//                contents = newContents;
//            }
//            buffer.get(0, bytesToWrite, 0, (int) bufSize);
//            contents.position((int) writeOffset);
//            contents.put(bytesToWrite);
//            contents.position(0); // Rewind
//        }

        /* Now save it to Bundle system */

        return (int) bufSize;
    }
    @Override
    protected void rename(String newFilePath) {
        this.getMetadataClient().renameFile(getFullPath(), newFilePath);
    }
}
