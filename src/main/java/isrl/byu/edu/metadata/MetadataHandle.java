package isrl.byu.edu.metadata;


import ru.serce.jnrfuse.struct.FileStat;

public class MetadataHandle {

    private long mode;
    private long userId;
    private long groupId;
    private long size;
    private long accessTime;
    private long creationTime;
    private long updatedTime;

    public MetadataHandle() {

    }

    public long getAccessTime() {
        return accessTime;
    }

    public long getMode() {
        return mode;
    }

    public long getUserId() {
        return userId;
    }

    public long getGroupId() {
        return groupId;
    }

    public long getSize() {
        return size;
    }

    public long getCreationTime() {
        return creationTime;
    }

    public long getUpdatedTime() {
        return updatedTime;
    }

    public void mutateFileStat(FileStat fileStat) {
        // I'm just guessing how the timestamps might work.
        fileStat.st_birthtime.tv_nsec.set(getCreationTime());
        fileStat.st_atim.tv_nsec.set(getAccessTime());
        fileStat.st_ctim.tv_nsec.set(getUpdatedTime());
        fileStat.st_mtim.tv_nsec.set(getUpdatedTime());

        fileStat.st_uid.set(getUserId());
        fileStat.st_gid.set(getGroupId());
        fileStat.st_nlink.set(1);
        fileStat.st_size.set(getSize());
        fileStat.st_mode.set(getMode());
    }

    public MetadataHandle setMode(long mode) {
        this.mode = mode;
        return this;
    }

    public MetadataHandle setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    public MetadataHandle setGroupId(long groupId) {
        this.groupId = groupId;
        return this;
    }

    public MetadataHandle setSize(long size) {
        this.size = size;
        return this;
    }

    public MetadataHandle setAccessTime(long accessTime) {
        this.accessTime = accessTime;
        return this;
    }

    public MetadataHandle setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public MetadataHandle setUpdatedTime(long updatedTime) {
        this.updatedTime = updatedTime;
        return this;
    }

}
