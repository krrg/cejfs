package isrl.byu.edu.storage;

import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.io.FileNotFoundException;
import java.net.ConnectException;
import java.nio.file.NoSuchFileException;

public class RedisMetadataStorage implements IMetadataStorage {

    private PendingMetadataActions pendingActions = new PendingMetadataActions();
    private RedisClient redis = null;

    public RedisMetadataStorage(String redisConnectionUri) {
        redis = RedisClient.create(redisConnectionUri);
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

        StatefulRedisConnection<String,String> connection = redis.connect();
        RedisCommands<String, String> commands = connection.sync();
        String oldValue = commands.get(key);
        commands.set(key, value);
        return oldValue;
    }

    @Override
    public String readMetadata(String key) throws NoSuchFieldException, ConnectException {
        RedisCommands<String, String> commands = redis.connect().sync();
        String value = commands.get(key);
        return value;
    }

    @Override
    public String deleteMetadata(String key) throws ConnectException {
        StatefulRedisConnection<String,String> connection = redis.connect();
        RedisCommands<String, String> commands = connection.sync();
        String oldValue = commands.get(key);
        commands.del(key);
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
