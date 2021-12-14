package ru.spbstu.datadeduplicationapp.transform;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InputStreamSplitterTest {
  private final InputStreamSplitter splitter = new InputStreamSplitter();
  @Test
  public void testSplitText() throws IOException {
    String text = "Text here about something";
    byte[] segment2 = new byte[16];
    Assertions.assertThat(splitter.splitIntoSegments(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))
        .containsExactly(
            "Text here about ".getBytes(StandardCharsets.UTF_8),
            "something       ".getBytes(StandardCharsets.UTF_8));
  }
}
