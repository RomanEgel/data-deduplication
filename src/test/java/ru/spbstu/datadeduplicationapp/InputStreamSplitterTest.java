package ru.spbstu.datadeduplicationapp;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.spbstu.datadeduplicationapp.transform.InputStreamSplitter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class InputStreamSplitterTest {
  private final InputStreamSplitter splitter = new InputStreamSplitter();
  @Test
  public void testSplitText() throws IOException {
    String text = "Text here about something";
    Assertions.assertThat(splitter.splitIntoSegments(new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8))))
      .containsExactly("Text".getBytes(StandardCharsets.UTF_8), " her".getBytes(StandardCharsets.UTF_8),
          "e ab".getBytes(StandardCharsets.UTF_8), "out ".getBytes(StandardCharsets.UTF_8), "some".getBytes(StandardCharsets.UTF_8),
          "thin".getBytes(StandardCharsets.UTF_8), "g".getBytes(StandardCharsets.UTF_8));
  }
}
