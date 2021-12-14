package ru.spbstu.datadeduplicationapp.controller;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.spbstu.datadeduplicationapp.BaseTest;
import ru.spbstu.datadeduplicationapp.model.Hash;
import ru.spbstu.datadeduplicationapp.persistence.HexUtils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class DataStorageControllerTest extends BaseTest {

  @Test
  public void testSimpleTextStorage() throws NoSuchAlgorithmException, IOException {
    String text = "Text here about something";
    ResponseEntity<String> result = testRestTemplate.postForEntity("/text", text, String.class);
    Assertions.assertThat(result.getStatusCode())
        .isEqualTo(HttpStatus.OK);
    String fileId = result.getBody();
    Assertions.assertThat(fileId)
        .isNotNull();

    ResponseEntity<String> data = testRestTemplate.getForEntity("/text/" + fileId, String.class);
    Assertions.assertThat(data.getStatusCode())
        .isEqualTo(HttpStatus.OK);
    Assertions.assertThat(data.getBody())
        .isEqualTo(text);

    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] hashValue = md.digest("Text here about ".getBytes(StandardCharsets.UTF_8));
    Hash firstHash = new Hash().setFilename(fileId).setHashValue(hashValue).setLineNumber(0L).setRepeatCount(0L).setReference(1L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(firstHash);
    Assertions.assertThat(hashRepository.getByReference(1L))
        .isEqualTo(firstHash);

    hashValue = md.digest("something       ".getBytes(StandardCharsets.UTF_8));
    Hash secondHash = new Hash().setFilename(fileId).setHashValue(hashValue).setLineNumber(1L).setRepeatCount(0L).setReference(2L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(secondHash);
    Assertions.assertThat(hashRepository.getByReference(2L))
        .isEqualTo(secondHash);

    List<String> lines = Files.readAllLines(Paths.get(persistenceDir, fileId));
    Assertions.assertThat(lines)
        .hasSize(2);
    Assertions.assertThat(lines.get(0))
        .isEqualTo(HexUtils.toHexString("Text here about ".getBytes(StandardCharsets.UTF_8)));
    Assertions.assertThat(lines.get(1))
        .isEqualTo(HexUtils.toHexString("something       ".getBytes(StandardCharsets.UTF_8)));
  }

  @Test
  public void testSimpleTextDeduplication() throws NoSuchAlgorithmException, IOException {
    String text1 = "Text here about something";
    String fileId1 = testRestTemplate.postForEntity("/text", text1, String.class).getBody();

    String text2 = "Text here about something else";
    String fileId2 = testRestTemplate.postForEntity("/text", text2, String.class).getBody();

    String text3 = "Text also about something";
    String fileId3 = testRestTemplate.postForEntity("/text", text3, String.class).getBody();

    Assertions.assertThat(testRestTemplate.getForEntity("/text/" + fileId1, String.class).getBody())
        .isEqualTo(text1);
    Assertions.assertThat(testRestTemplate.getForEntity("/text/" + fileId2, String.class).getBody())
        .isEqualTo(text2);
    Assertions.assertThat(testRestTemplate.getForEntity("/text/" + fileId3, String.class).getBody())
        .isEqualTo(text3);

    MessageDigest md = MessageDigest.getInstance("MD5");
    byte[] hashValue = md.digest("Text here about ".getBytes(StandardCharsets.UTF_8));
    Hash firstHash = new Hash().setFilename(fileId1).setHashValue(hashValue).setLineNumber(0L).setRepeatCount(1L).setReference(1L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(firstHash);
    Assertions.assertThat(hashRepository.getByReference(1L))
        .isEqualTo(firstHash);

    hashValue = md.digest("something       ".getBytes(StandardCharsets.UTF_8));
    Hash secondHash = new Hash().setFilename(fileId1).setHashValue(hashValue).setLineNumber(1L).setRepeatCount(1L).setReference(2L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(secondHash);
    Assertions.assertThat(hashRepository.getByReference(2L))
        .isEqualTo(secondHash);

    hashValue = md.digest("something else  ".getBytes(StandardCharsets.UTF_8));
    Hash thirdHash = new Hash().setFilename(fileId2).setHashValue(hashValue).setLineNumber(1L).setRepeatCount(0L).setReference(3L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(thirdHash);
    Assertions.assertThat(hashRepository.getByReference(3L))
        .isEqualTo(thirdHash);

    hashValue = md.digest("Text also about ".getBytes(StandardCharsets.UTF_8));
    Hash fourthHash = new Hash().setFilename(fileId3).setHashValue(hashValue).setLineNumber(0L).setRepeatCount(0L).setReference(4L);
    Assertions.assertThat(hashRepository.get(hashValue))
        .isEqualTo(fourthHash);
    Assertions.assertThat(hashRepository.getByReference(4L))
        .isEqualTo(fourthHash);


    List<String> lines = Files.readAllLines(Paths.get(persistenceDir, fileId1));
    Assertions.assertThat(lines)
        .hasSize(2);
    Assertions.assertThat(lines.get(0))
        .isEqualTo(HexUtils.toHexString("Text here about ".getBytes(StandardCharsets.UTF_8)));
    Assertions.assertThat(lines.get(1))
        .isEqualTo(HexUtils.toHexString("something       ".getBytes(StandardCharsets.UTF_8)));

    lines = Files.readAllLines(Paths.get(persistenceDir, fileId2));
    Assertions.assertThat(lines)
        .hasSize(2);
    Assertions.assertThat(lines.get(0))
        .isEqualTo(HexUtils.toHexString(ByteBuffer.allocate(Long.BYTES).putLong(1L).array()));
    Assertions.assertThat(lines.get(1))
        .isEqualTo(HexUtils.toHexString("something else  ".getBytes(StandardCharsets.UTF_8)));

    lines = Files.readAllLines(Paths.get(persistenceDir, fileId3));
    Assertions.assertThat(lines)
        .hasSize(2);
    Assertions.assertThat(lines.get(0))
        .isEqualTo(HexUtils.toHexString("Text also about ".getBytes(StandardCharsets.UTF_8)));
    Assertions.assertThat(lines.get(1))
        .isEqualTo(HexUtils.toHexString(ByteBuffer.allocate(Long.BYTES).putLong(2L).array()));

  }
}
