package isrl.byu.edu;

import isrl.byu.edu.bundle.IBundleClient;
import isrl.byu.edu.metadata.IMetadataClient;
import jnr.ffi.Pointer;
import ru.serce.jnrfuse.struct.FileStat;
import ru.serce.jnrfuse.struct.FuseContext;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

public class FileProxy extends FusePath {

    private IMetadataClient metadataClient;
    private IBundleClient bundleClient;
    private FuseContext fuseContext;

    FileProxy(String name, ProxyParameters proxyParameters) {
        super(name, proxyParameters);
    }

    FileProxy(String name, DirectoryProxy parent, ProxyParameters proxyParameters) {
        super(name, parent, proxyParameters);
    }

    FileProxy(String name, String text, ProxyParameters parameters) {
        super(name, parameters);
        try {
            byte[] contentBytes = text.getBytes("UTF-8");
            bundleClient.saveFile(contentBytes, name);
        } catch (UnsupportedEncodingException e) {
            // Not going to happen
        }
    }

    @Override
    protected void getattr(FileStat stat) {
        stat.st_mode.set(FileStat.S_IFREG | 0777);
        stat.st_size.set(contents.capacity());
        stat.st_uid.set(getContext().uid.get());
        stat.st_gid.set(getContext().pid.get());
    }

    int read(Pointer buffer, long size, long offset) {
        int bytesToRead = (int) Math.min(contents.capacity() - offset, size);
        byte[] bytesRead = new byte[bytesToRead];
        synchronized (this) {
            contents.position((int) offset);
            contents.get(bytesRead, 0, bytesToRead);
            try {
                bundleClient.readFile(this.getName());
                buffer.put(0, bytesRead, 0, bytesToRead);
            } catch (IOException e) {

            }
            contents.position(0); // Rewind
        }
        return bytesToRead;
    }

    synchronized void truncate(long size) {
        if (size < contents.capacity()) {
            // Need to create a new, smaller buffer
            ByteBuffer newContents = ByteBuffer.allocate((int) size);
            byte[] bytesRead = new byte[(int) size];
            contents.get(bytesRead);
            newContents.put(bytesRead);
            contents = newContents;
        }
    }

    int write(Pointer buffer, long bufSize, long writeOffset) {
        int maxWriteIndex = (int) (writeOffset + bufSize);
        byte[] bytesToWrite = new byte[(int) bufSize];
        synchronized (this) {
            if (maxWriteIndex > contents.capacity()) {
                // Need to create a new, larger buffer
                ByteBuffer newContents = ByteBuffer.allocate(maxWriteIndex);
                newContents.put(contents);
                contents = newContents;
            }
            buffer.get(0, bytesToWrite, 0, (int) bufSize);
            contents.position((int) writeOffset);
            contents.put(bytesToWrite);
            contents.position(0); // Rewind
        }

        /* Now save it to Bundle system */

        return (int) bufSize;
    }
}
