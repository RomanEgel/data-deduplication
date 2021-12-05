package ru.spbstu.datadeduplicationapp.controller;

import lombok.RequiredArgsConstructor;
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
import java.util.function.Supplier;

@RestController
@RequiredArgsConstructor
public class DataStorageController {
  private final InputStreamSplitter inputStreamSplitter;
  private final SegmentsCacheConnector cacheConnector;
  private final SegmentsPersistenceService persistenceService;

  @PostMapping("/store-text-file")
  public String storeTextFile(@RequestParam("file") MultipartFile file) {
    try (InputStream is = file.getInputStream()) {
      return storeTextInputStream(is);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }

  @PostMapping(value = "/store-text", consumes = "plain/text")
  public String storeText(@RequestBody String text) {
    try {
      ByteArrayInputStream is = new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
      return storeTextInputStream(is);
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }


  private String storeTextInputStream(InputStream textInputStream) throws IOException {
    List<byte[]> segments = inputStreamSplitter.splitIntoSegments(textInputStream);
    List<byte[]> cachedSegments = cacheConnector.applyCacheToSegments(segments);
    return persistenceService.persistSegments(cachedSegments);
  }
}
