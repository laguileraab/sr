package com.ce.sr.repository;

import java.util.Optional;

import com.ce.sr.models.FileMetadata;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface FileRepository extends MongoRepository<FileMetadata, String> {
    
    Optional<FileMetadata> findByFileId(String id);

    void deleteByFileId(String id);

}
