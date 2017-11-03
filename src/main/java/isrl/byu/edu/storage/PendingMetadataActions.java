package isrl.byu.edu.storage;

import isrl.byu.edu.bundle.Bundle;

import java.util.HashSet;

public class PendingMetadataActions {

    private HashSet<String> pendingDelete = new HashSet<>();
    private HashSet<MetadataTuple> pendingWrite= new HashSet<>();

    ///////// queuing to delete dead metadata on remote////////

    public void queueDelete(String key) {
        this.pendingDelete.add(key);
    }

    public void queueDeletes(HashSet<String> keys) {
        this.pendingDelete.addAll(keys);
    }

    public String popDelete() {
        if(this.pendingDelete.iterator().hasNext()) {
            String mapping = this.pendingDelete.iterator().next();
            this.pendingDelete.remove(mapping);
            return mapping;
        }
        return null;
    }

    ///////// queuing and writing metadata to remote////////
    public void queueWrite(MetadataTuple metadataTuple) {
        this.pendingWrite.add(metadataTuple);

    }
    public void queueWrites(HashSet<MetadataTuple> metadataTuples) {
        this.pendingWrite.addAll(metadataTuples);
    }

    public MetadataTuple popWrite() {
        if(this.pendingWrite.iterator().hasNext()) {
            MetadataTuple metadataTuple = this.pendingWrite.iterator().next();
            this.pendingWrite.remove(metadataTuple);
            return metadataTuple;
        }
        return null;
    }
}
