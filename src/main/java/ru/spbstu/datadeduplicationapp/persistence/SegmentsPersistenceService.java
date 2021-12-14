package ru.spbstu.datadeduplicationapp.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class SegmentsPersistenceService {
  @Value("${file.persistence.path}")
  private String persistenceDir;

  public String persistSegments(String path, List<byte[]> segmentsToPersist) throws IOException {
    List<String> hexSegments = new ArrayList<>();
    segmentsToPersist.forEach(
        segment -> hexSegments.add(HexUtils.toHexString(segment))
    );

    Files.write(Paths.get(persistenceDir, path), hexSegments, StandardOpenOption.CREATE_NEW);
    return path;
  }

  public List<byte[]> readSegments(String path) throws IOException {
    File file = Paths.get(persistenceDir, path).toFile();
    if (!file.exists()) {
      throw new RuntimeException(String.format("Files %s doesn't exist", path));
    }

    List<byte[]> segments = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        segments.add(HexUtils.toByteArray(line));
      }
    }

    return segments;
  }

  public byte[] readSingleSegment(String path, Long lineNumber) {
    try (Stream<String> lines = Files.lines(Paths.get(persistenceDir, path))) {
      return HexUtils.toByteArray(lines.skip(lineNumber).findFirst().orElseThrow());
    } catch (IOException ioException) {
      throw new RuntimeException("Error while reading file " + path);
    }
  }


}
