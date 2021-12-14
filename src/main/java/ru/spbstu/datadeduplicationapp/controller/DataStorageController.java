package ru.spbstu.datadeduplicationapp.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.spbstu.datadeduplicationapp.caching.SegmentsCacheConnector;
import ru.spbstu.datadeduplicationapp.persistence.SegmentsPersistenceService;
import ru.spbstu.datadeduplicationapp.transform.InputStreamSplitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class DataStorageController {
  private final InputStreamSplitter inputStreamSplitter;
  private final SegmentsCacheConnector cacheConnector;
  private final SegmentsPersistenceService persistenceService;

  @PostMapping("/text-file")
  public String storeTextFile(@RequestParam("file") MultipartFile file) {
    try (InputStream is = file.getInputStream()) {
      return storeTextInputStream(is);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @PostMapping(value = "/text", consumes = "text/plain")
  public String storeText(@RequestBody String text) {
    try {
      ByteArrayInputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
      return storeTextInputStream(is);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @GetMapping("/text/{id}")
  public String readText(@PathVariable String id) {
    try {
      return composeOutputTextFromFile(id);
    } catch (IOException ioException) {
      throw new RuntimeException();
    }
  }

  private String storeTextInputStream(InputStream textInputStream) throws IOException {
    String path = UUID.randomUUID().toString();
    List<byte[]> segments = inputStreamSplitter.splitIntoSegments(textInputStream);
    List<byte[]> cachedSegments = cacheConnector.applyCacheToSegments(path, segments);
    return persistenceService.persistSegments(path, cachedSegments);
  }

  private String composeOutputTextFromFile(String id) throws IOException {
    List<byte[]> segments = persistenceService.readSegments(id);
    List<byte[]> resolvedSegments = cacheConnector.resolveReferences(segments);

    return inputStreamSplitter.composeToOutputText(resolvedSegments);
  }
}
