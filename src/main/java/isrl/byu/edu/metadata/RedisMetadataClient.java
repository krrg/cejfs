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
    public Optional<MetadataHandle> createFile(Path path) {
        RedisCommands<String, String> commands = redis.connect().sync();
        if (commands.get(path.toString()) != null) {
            return Optional.empty();
        }

        throw new UnsupportedOperationException();

    }

    @Override
    public Optional<MetadataHandle> createDirectory(Path path) {
        return null;
    }

    @Override
    public boolean deleteMetadata(Path path) {
        return false;
    }

    @Override
    public Optional<MetadataHandle> getMetadata(Path path) {
        return null;
    }


    private Optional<MetadataHandle> getMetadata(Path path, StatefulRedisConnection<String, String> redisConnection) {
        redisConnection.sync().get()
    }
}
