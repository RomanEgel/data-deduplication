package ru.spbstu.datadeduplicationapp.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class SegmentsPersistenceService {

  public String persistSegments(List<byte[]> segmentsToPersist) {

    return "void";
  }
}
