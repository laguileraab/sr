package com.ce.sr.models;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import nonapi.io.github.classgraph.json.Id;

@Getter
@Setter
@NoArgsConstructor
@Document(collection = "fileMetadata")
public class FileMetadata {
        @Id
        private String id;
        private String fileId;
        private String filename;
        private String owner;
}
