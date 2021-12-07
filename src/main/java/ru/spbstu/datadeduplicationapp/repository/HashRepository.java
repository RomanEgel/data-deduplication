package ru.spbstu.datadeduplicationapp.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.spbstu.datadeduplicationapp.model.Hash;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Repository
public class HashRepository {
    private HashOperations hashOperations;
    private final String FILENAME_KEY = "filename";
    private final String LINE_NUMBER_KEY = "lineNumber";
    private final String REPEAT_COUNT_KEY = "repeatCount";

    public HashRepository(RedisTemplate redisTemplate) {
        this.hashOperations = redisTemplate.opsForHash();
    }

    public void create(Hash hash) {
        hashOperations.putAll(hash.getHashValue(),
                Map.of(FILENAME_KEY, hash.getFilename(),
                        LINE_NUMBER_KEY, hash.getLineNumber().toString(),
                        REPEAT_COUNT_KEY, hash.getRepeatCount().toString()));

    }

    public Hash get(String hashValue) {
        List<String> fields = hashOperations.multiGet(hashValue,
                Arrays.asList(FILENAME_KEY, LINE_NUMBER_KEY, REPEAT_COUNT_KEY));
        if (!fields.contains(null))
            return new Hash(hashValue,
                    fields.get(0),
                    Long.parseLong(fields.get(1)),
                    Long.parseLong(fields.get(2)));
        else
            return null;
    }

    public void delete(String hashValue) {
        hashOperations.delete(hashValue, FILENAME_KEY, LINE_NUMBER_KEY, REPEAT_COUNT_KEY);
    }

    public boolean isHashExists(String hashValue) {
        return !(hashOperations.keys(hashValue).isEmpty());
    }

    public void incrementRepeatCount(String hashValue) {
        hashOperations.increment(hashValue, REPEAT_COUNT_KEY, 1);
    }

    public void decrementRepeatCount(String hashValue) {
        hashOperations.increment(hashValue, REPEAT_COUNT_KEY, -1);
    }
}
