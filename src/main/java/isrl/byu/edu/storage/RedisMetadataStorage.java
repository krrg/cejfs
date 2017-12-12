package isrl.byu.edu.storage;

import io.lettuce.core.KeyValue;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;
import java.util.List;

public class RedisMetadataStorage implements IMetadataStorage {

    private PendingMetadataActions pendingActions = new PendingMetadataActions();
    private RedisClient redis = null;
    private StatefulRedisConnection<String, String> connection = null;
    private String BUNDLERKEY = "...BUNDLER...";
    public RedisMetadataStorage(String redisConnectionUri) {

        redis = RedisClient.create(redisConnectionUri);
        connection = redis.connect();
    }

    @Override
    public PendingMetadataActions getPendingActions() {
        return pendingActions;
    }

    @Override
    public String getID() {
        return "redisMetadata";
    }

    @Override
    public String writeMetadata(String key, String value) throws ConnectException {

        RedisCommands<String, String> commands = connection.sync();
        String oldValue = commands.get(BUNDLERKEY+key);
        commands.set(BUNDLERKEY+key, value);
        return oldValue;
    }

    @Override
    public String readMetadata(String key) throws NoSuchFieldException, ConnectException {
        RedisCommands<String, String> commands = connection.sync();
        String value = commands.get(BUNDLERKEY+key);
        return value;
    }

    @Override
    public String deleteMetadata(String key) throws ConnectException {
        RedisCommands<String, String> commands = connection.sync();
        String oldValue = commands.get(BUNDLERKEY+key);
        commands.del(BUNDLERKEY+key);
        return oldValue;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        if (getClass() != other.getClass()) {
            return false;
        }
        RedisMetadataStorage otherStorage = (RedisMetadataStorage) other;
        if (this.getID() != otherStorage.getID()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode()
    {
        return this.getID().hashCode();
    }


}
