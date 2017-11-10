package isrl.byu.edu.metadata;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.sync.RedisCommands;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.UUID;


public class RedisMetadataClient implements IMetadataClient {

    private RedisClient redis = null;

    public RedisMetadataClient(String redisConnectionUri) {
        redis = RedisClient.create(redisConnectionUri);

    }

    public boolean isDirectory(String name) {
        return false;
    }


    public boolean isSubDirectory(String parent, String child) {
        return false;
    }

    public Optional<MetadataHandle> getMetadata(String name) {
        RedisCommands<String, String> rediscx = redis.connect().sync();

        throw new UnsupportedOperationException("Not yet implemented.");
    }

    @Override
    public boolean add(String parent, String child, MetadataHandle handle) {
        RedisCommands<String, String> rediscx = redis.connect().sync();

        String existingChildMetadataKey = rediscx.hget(parent, child);
        if (existingChildMetadataKey != null) {
            throw new UnsupportedOperationException("Already exists.  Also this is the wrong exception :)");
        }

        String newChildMetadataKey = child + "(" + UUID.randomUUID().toString() + ")";

        rediscx.multi();
        rediscx.hset(newChildMetadataKey, "mode", Long.toHexString(handle.getMode()));
        rediscx.hset(newChildMetadataKey, "userId", Long.toHexString(handle.getUserId()));
        rediscx.hset(newChildMetadataKey, "groupId", Long.toHexString(handle.getGroupId()));
        rediscx.hset(newChildMetadataKey, "size", Long.toHexString(handle.getSize()));
        rediscx.hset(newChildMetadataKey, "accessTime", Long.toHexString(handle.getAccessTime()));
        rediscx.hset(newChildMetadataKey, "creationTime", Long.toHexString(handle.getCreationTime()));
        rediscx.hset(newChildMetadataKey, "updatedTime", Long.toHexString(handle.getUpdatedTime()));
        rediscx.exec();

        return true;
    }

    @Override
    public boolean remove(String parent, String child) {
        // Is it a directory?
        String key = parent + "/" + child;

        if (! this.listChildren(key).isEmpty()) {

        }

        return false;
    }

    @Override
    public List<MetadataHandle> listChildren(String parent) {
        return null;
    }
}
