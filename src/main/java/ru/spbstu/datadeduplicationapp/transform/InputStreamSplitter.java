package ru.spbstu.datadeduplicationapp.transform;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class InputStreamSplitter {
  private static final int SEGMENT_SIZE_IN_BYTES = 4;

  public List<byte[]> splitIntoSegments(InputStream inputStream) throws IOException {
    byte[] segment = new byte[SEGMENT_SIZE_IN_BYTES];
    int len;
    List<byte[]> segments = new ArrayList<>();

    while ((len = inputStream.read(segment)) != 0) {
      segments.add(Arrays.copyOf(segment, len));

      if (len < SEGMENT_SIZE_IN_BYTES) {
        break;
      }
    }

    return segments;
  }
}
