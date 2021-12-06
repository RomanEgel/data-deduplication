package ru.spbstu.datadeduplicationapp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hash implements Serializable {
    private String hashValue;
    private String filename;
    private Long lineNumber;
    private Long repeatCount;

    @Override
    public String toString() {
        return "hashValue: " + hashValue + "; "
                + "filename: " + filename + "; "
                + "lineNumber: " + lineNumber + "; "
                + "repeatCount: " + repeatCount + "; ";
    }
}