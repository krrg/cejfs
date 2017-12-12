package isrl.byu.edu.metadata;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import isrl.byu.edu.utils.FilePathUtils;

import javax.swing.text.html.Option;
import java.util.*;


public class RedisMetadataClient implements IMetadataClient {

    private RedisClient redis = null;
    private StatefulRedisConnection<String, String> connection = null;

    public RedisMetadataClient(String redisConnectionUri) {
        redis = RedisClient.create(redisConnectionUri);
        connection = redis.connect();
    }

    public Optional<MetadataHandle> getMetadata(String name) {
        RedisCommands<String, String> rediscx = connection.sync();
        Map<String, String> metadataMap = rediscx.hgetall(name);

        System.out.println("Metadata mpa" + metadataMap);
        if (metadataMap == null || metadataMap.isEmpty()) {
            return Optional.empty();
        }

        try {

            long mode = Long.parseLong(metadataMap.getOrDefault("mode", "0"));
            long userId = Long.parseLong(metadataMap.getOrDefault("userId", "0"));
            long groupId = Long.parseLong(metadataMap.getOrDefault("groupId", "0"));
            long size = Long.parseLong(metadataMap.getOrDefault("size", "0"));
            long accessTime = Long.parseLong(metadataMap.getOrDefault("accessTime", "0"));
            long creationTime = Long.parseLong(metadataMap.getOrDefault("creationTime", "0"));
            long updatedTime = Long.parseLong(metadataMap.getOrDefault("updatedTime", "0"));

            MetadataHandle metadataHandle = new MetadataHandle(mode, userId, groupId);
            metadataHandle.setSize(size);
            metadataHandle.setAccessTime(accessTime);
            metadataHandle.setCreationTime(creationTime);
            metadataHandle.setUpdatedTime(updatedTime);

            return Optional.of(metadataHandle);

        } catch (Exception e) {
            System.out.println("I just caught an exception." + e);
            e.printStackTrace();
            return Optional.empty();
        }
    }

    @Override
    public boolean addChild(String fullPath, MetadataHandle handle) {

        System.err.println("Trying to print full path" + fullPath);

        String parent = "parent:" + FilePathUtils.getParentFullPath(fullPath);
        String child = FilePathUtils.getFileName(fullPath);

        RedisCommands<String, String> rediscx = connection.sync();

        if (rediscx.sismember(parent, child)) {
            System.err.println("File already exists.");
            return false;
        }

        rediscx.sadd(parent, child);

        rediscx.multi();
        rediscx.hset(fullPath, "mode", Long.toString(handle.getMode()));
        rediscx.hset(fullPath, "userId", Long.toString(handle.getUserId()));
        rediscx.hset(fullPath, "groupId", Long.toString(handle.getGroupId()));
        rediscx.hset(fullPath, "size", Long.toString(handle.getSize()));
        rediscx.hset(fullPath, "accessTime", Long.toString(handle.getAccessTime()));
        rediscx.hset(fullPath, "creationTime", Long.toString(handle.getCreationTime()));
        rediscx.hset(fullPath, "updatedTime", Long.toString(handle.getUpdatedTime()));
        rediscx.exec();

        System.err.println("Returning true!");

        return true;
    }

    @Override
    public boolean removeChild(String fullPath) {
        RedisCommands<String, String> rediscx = connection.sync();

        String parent = "parent:" + FilePathUtils.getParentFullPath(fullPath);
        String child = FilePathUtils.getFileName(fullPath);

        return rediscx.srem(parent, child) > 0;
    }

    @Override
    public boolean renameFolder(String fullPath, String newName) {
        throw new UnsupportedOperationException("I don't know how to rename things.");
    }

    @Override
    public Collection<String> listChildren(String parent) {
        System.out.println("Looking at parent: " + parent);
        return connection.sync().smembers("parent:" + parent);
    }

    @Override
    public boolean renameFile(String fullPath, String newName) {
        throw new UnsupportedOperationException("I don't know how to rename things.");
    }
}
