package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;

public class FileProxy extends FusePath {

    FileProxy(String name, ProxyParameters proxyParameters) {
        super(name, proxyParameters);
    }

    FileProxy(String name, DirectoryProxy parent, ProxyParameters proxyParameters) {
        super(name, parent, proxyParameters);
    }

    @Override
    protected void getattr(FileStat stat) {
//        getMetadataClient().getMetadata(this.getFullName())
//            .orElseThrow(RuntimeException::new)
//            .mutateFileStat(stat);

        stat.st_mode.set(FileStat.S_IFREG | 0777);
        stat.st_size.set(getBundleClient().readFile(getFullName()).length);
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().pid.get());
    }

    int read(Pointer buffer, long size, long offset) {
        byte [] fullFile = getBundleClient().readFile(this.getFullName());
        int actualSize = (int)Math.min(fullFile.length, size);

        buffer.put(offset, fullFile, (int)offset, actualSize);
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
            throw new UnsupportedOperationException("Write offset cannot be non-zero");
        }

        byte [] allocated = new byte[(int) bufSize];
        buffer.get(0, allocated, 0, (int) bufSize);
        getBundleClient().saveFile(allocated, this.getFullName());

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
}
