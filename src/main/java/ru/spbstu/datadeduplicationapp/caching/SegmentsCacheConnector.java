package ru.spbstu.datadeduplicationapp.caching;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.spbstu.datadeduplicationapp.DeduplicationConsts;
import ru.spbstu.datadeduplicationapp.model.Hash;
import ru.spbstu.datadeduplicationapp.persistence.SegmentsPersistenceService;
import ru.spbstu.datadeduplicationapp.repository.HashRepository;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SegmentsCacheConnector {
  private final HashRepository hashRepository;
  private final SegmentsPersistenceService persistenceService;

  public List<byte[]> applyCacheToSegments(String path, List<byte[]> segments) {
    try {
      MessageDigest md = MessageDigest.getInstance("MD5");
      int segmentCount = 0;

      for (byte[] segment : segments) {
        byte[] segmentHash = md.digest(segment);
        int finalSegmentCount = segmentCount;
        Long segmentRef = hashRepository.createOrReturnReference(segmentHash, () -> new Hash()
            .setHashValue(segmentHash)
            .setFilename(path)
            .setLineNumber((long) finalSegmentCount)
            .setRepeatCount(0L));

        if (segmentRef != null) {
          segments.set(segmentCount, ByteBuffer.allocate(DeduplicationConsts.REFERENCE_SIZE).putLong(segmentRef).array());
          hashRepository.incrementRepeatCount(segmentHash);
        }

        segmentCount++;
      }
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("No such algorithm", e);
    }

    return segments;
  }

  public List<byte[]> resolveReferences(List<byte[]> segments) {
    List<byte[]> resolvedSegments = new ArrayList<>();

    for (byte[] segment : segments) {
      if (segment.length == DeduplicationConsts.SEGMENT_SIZE_IN_BYTES) {
        resolvedSegments.add(segment);
      } else {
        Hash hash = hashRepository.getByReference(ByteBuffer.wrap(segment).getLong());
        // TODO THINK ABOUT AGGREGATING queries to the same file
        resolvedSegments.add(persistenceService.readSingleSegment(hash.getFilename(), hash.getLineNumber()));
      }
    }

    return resolvedSegments;
  }
}
