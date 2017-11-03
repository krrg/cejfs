package isrl.byu.edu.metadata;

import io.lettuce.core.RedisClient;
import io.lettuce.core.StatefulRedisConnectionImpl;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;
import io.lettuce.core.resource.ClientResources;

import java.nio.file.Path;
import java.util.Optional;


public class RedisMetadataClient implements IMetadataClient {

    private RedisClient redis = null;

    public RedisMetadataClient(String redisConnectionUri) {
        redis = RedisClient.create(redisConnectionUri);

    }

    @Override
    public Optional<MetadataHandle> createFile(String path) {
        RedisCommands<String, String> commands = redis.connect().sync();
        if (commands.get(path) != null) {
            MetadataHandle metadataHandle = new MetadataHandle();
            metadataHandle.set
        }

        return Optional.empty();
    }

    @Override
    public Optional<MetadataHandle> createDirectory(String path) {
        RedisCommands<String, String> commands = redis.connect().sync();
    }

    @Override
    public boolean deleteMetadata(String path) {
        return false;
    }

    @Override
    public Optional<MetadataHandle> getMetadata(String path) {
        return null;
    }

}
