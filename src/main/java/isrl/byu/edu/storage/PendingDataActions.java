package isrl.byu.edu.storage;

import isrl.byu.edu.bundle.Bundle;

import java.util.HashMap;
import java.util.HashSet;

public class PendingDataActions {

    private HashSet<String> pendingDelete = new HashSet<>();
    private HashSet<FileTuple> pendingWrite= new HashSet<>();

    ///////// queuing to delete dead data on remote////////

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

    ///////// queuing and pushing fileTuple to remote////////
    public void queueWrite(FileTuple fileTuple) {
        this.pendingWrite.add(fileTuple);

    }
    public void queueWrites(HashSet<FileTuple> fileTuples) {
        this.pendingWrite.addAll(fileTuples);
    }

    public FileTuple popWrite() {
        if(this.pendingWrite.iterator().hasNext()) {
            FileTuple fileTuple = this.pendingWrite.iterator().next();
            this.pendingWrite.remove(fileTuple);
            return fileTuple;
        }
        return null;
    }
}
