package ru.spbstu.datadeduplicationapp.repository;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import ru.spbstu.datadeduplicationapp.model.Hash;

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
        hashOperations.put(hash.getHashValue(), FILENAME_KEY, hash.getFilename());
        hashOperations.put(hash.getHashValue(), LINE_NUMBER_KEY, hash.getLineNumber().toString());
        hashOperations.put(hash.getHashValue(), REPEAT_COUNT_KEY, hash.getRepeatCount().toString());
    }

    public Hash get(String hashValue) {
        String filename = (String) hashOperations.get(hashValue, FILENAME_KEY);
        String lineNumber = (String) hashOperations.get(hashValue, LINE_NUMBER_KEY);
        String repeatCount = (String) hashOperations.get(hashValue, REPEAT_COUNT_KEY);
        if (filename != null && lineNumber != null && repeatCount != null)
            return new Hash(hashValue, filename, Long.parseLong(lineNumber), Long.parseLong(repeatCount));
        else
            return null;
    }

    public void delete(String hashValue) {
        hashOperations.delete(hashValue, FILENAME_KEY);
        hashOperations.delete(hashValue, LINE_NUMBER_KEY);
        hashOperations.delete(hashValue, REPEAT_COUNT_KEY);
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
