package ru.spbstu.datadeduplicationapp.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;
import ru.spbstu.datadeduplicationapp.model.Hash;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Repository
public class HashRepository {
    private final HashOperations<byte[], byte[], String> hashOperations;
    private final ValueOperations<byte[], Object> valueOperations;
    private final byte[] FILENAME_KEY = "filename".getBytes(StandardCharsets.UTF_8);
    private final byte[] LINE_NUMBER_KEY = "lineNumber".getBytes(StandardCharsets.UTF_8);
    private final byte[] REPEAT_COUNT_KEY = "repeatCount".getBytes(StandardCharsets.UTF_8);
    private final byte[] REFERENCE_KEY = "reference".getBytes(StandardCharsets.UTF_8);
    private final byte[] HASH_ID_INC_KEY = "hashIdsIncrement".getBytes(StandardCharsets.UTF_8);

    public HashRepository(RedisTemplate<byte[], Object> redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
        this.valueOperations = redisTemplate.opsForValue();
    }

    public Long createOrReturnReference(byte[] hashValue, Supplier<Hash> hashSupplier) {
        RedisOperations<byte[], ?> basicOperations = hashOperations.getOperations();
        basicOperations.watch(hashValue);
        String referenceStr = hashOperations.get(hashValue, REFERENCE_KEY);

        if (referenceStr == null) {
            Long increment = valueOperations.increment(HASH_ID_INC_KEY);
            assert increment != null;

            basicOperations.multi();
            Hash hash = hashSupplier.get();
            hashOperations.putAll(hashValue,
                Map.of(FILENAME_KEY, hash.getFilename(),
                    LINE_NUMBER_KEY, hash.getLineNumber().toString(),
                    REPEAT_COUNT_KEY, hash.getRepeatCount().toString(),
                    REFERENCE_KEY, String.valueOf(increment)));
            valueOperations.set(ByteBuffer.allocate(Long.BYTES).putLong(increment).array(), hashValue);
            basicOperations.exec();
            return null;
        } else {
            basicOperations.unwatch();
            return Long.parseLong(referenceStr);
        }
    }

    public Hash get(byte[] hashValue) {
        List<String> fields = hashOperations.multiGet(hashValue,
            Arrays.asList(FILENAME_KEY, LINE_NUMBER_KEY, REPEAT_COUNT_KEY, REFERENCE_KEY));
        if (!fields.contains(null))
            return new Hash(hashValue,
                fields.get(0),
                Long.parseLong(fields.get(1)),
                Long.parseLong(fields.get(2)),
                Long.parseLong(fields.get(3)));
        else
            return null;
    }

    public void delete(byte[] hashValue) {
        hashOperations.getOperations().delete(hashValue);
    }

    public boolean isHashExists(byte[] hashValue) {
        return !(hashOperations.keys(hashValue).isEmpty());
    }

    public void incrementRepeatCount(byte[] hashValue) {
        hashOperations.increment(hashValue, REPEAT_COUNT_KEY, 1);
    }

    public void decrementRepeatCount(byte[] hashValue) {
        hashOperations.increment(hashValue, REPEAT_COUNT_KEY, -1);
    }

    public Hash getByReference(long reference) {
        Object hashValue = valueOperations.get(ByteBuffer.allocate(Long.BYTES).putLong(reference).array());
        assert hashValue instanceof byte[];

        return get((byte[]) hashValue);
    }
}
