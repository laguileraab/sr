package com.ce.sr.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileUpload {
    private String id;
    private String filename;
    private String fileType;
    private String fileSize;
    private byte[] file;
}
