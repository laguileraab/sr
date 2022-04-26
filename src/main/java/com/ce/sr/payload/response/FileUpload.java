package com.ce.sr.payload.response;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FileUpload {
    private String id;
    private String filename;
    private String fileType;
    private String fileSize;
    private String status;
    private byte[] file;
}
