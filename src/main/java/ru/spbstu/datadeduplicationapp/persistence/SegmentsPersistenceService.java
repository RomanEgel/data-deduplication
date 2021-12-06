package ru.spbstu.datadeduplicationapp.persistence;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SegmentsPersistenceService {
  @Value("${file.persistence.path}")
  private String persistenceDir;

  public String persistSegments(List<byte[]> segmentsToPersist) throws IOException {
    String path = UUID.randomUUID().toString();
    Files.write(
        Paths.get(persistenceDir, path),
        segmentsToPersist.stream().reduce(ArrayUtils::addAll).orElseThrow(),
        StandardOpenOption.CREATE_NEW);
    return path;
  }

  public String readText(String path) throws IOException {
    return Files.readString(Paths.get(persistenceDir, path), StandardCharsets.UTF_8);
  }
}
