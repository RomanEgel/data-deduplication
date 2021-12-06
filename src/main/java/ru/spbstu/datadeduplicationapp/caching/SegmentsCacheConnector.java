package ru.spbstu.datadeduplicationapp.caching;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SegmentsCacheConnector {

  public List<byte[]> applyCacheToSegments(List<byte[]> segments) {
    // TODO implement caching and deduplication
    return segments;
  }
}
