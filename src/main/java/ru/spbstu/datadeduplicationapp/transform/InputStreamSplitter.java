package ru.spbstu.datadeduplicationapp.transform;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static ru.spbstu.datadeduplicationapp.DeduplicationConsts.SEGMENT_SIZE_IN_BYTES;

@Component
public class InputStreamSplitter {

  public List<byte[]> splitIntoSegments(InputStream inputStream) throws IOException {
    byte[] segment = new byte[SEGMENT_SIZE_IN_BYTES];
    int len;
    List<byte[]> segments = new ArrayList<>();

    while ((len = inputStream.read(segment)) != 0) {
      byte[] segmentCopy = Arrays.copyOf(segment, SEGMENT_SIZE_IN_BYTES);
      segments.add(segmentCopy);

      if (len < SEGMENT_SIZE_IN_BYTES) {
        Arrays.fill(segmentCopy, len, SEGMENT_SIZE_IN_BYTES, (byte) ' ');
        break;
      }
    }

    return segments;
  }

  public String composeToOutputText(List<byte[]> segments) {
    StringBuilder sb = new StringBuilder();
    segments.forEach(
        s -> sb.append(new String(s, StandardCharsets.UTF_8))
    );

    return sb.toString().trim();
  }
}
