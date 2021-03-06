package ru.spbstu.datadeduplicationapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class Hash implements Serializable {
  private byte[] hashValue;
  private String filename;
    private Long lineNumber;
  private Long repeatCount;
  private Long reference;
}