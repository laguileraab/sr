package com.ce.sr.services;

import com.ce.sr.exceptions.FileForbiddenException;
import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.models.FileMetadata;
import com.ce.sr.payload.response.FileUpload;
import com.ce.sr.repository.FileRepository;
import com.ce.sr.utils.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;

import org.apache.commons.io.IOUtils;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Log4j2
@Service
public class FileService {

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    private FileRepository fileRepository;

    public String addFile(InputStream file, Long size, String name, String contentType) {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        DBObject metadata = new BasicDBObject();
        metadata.put(Constants.FILESIZE, size);
        metadata.put(Constants.STATUS, Constants.UPLOAD);
        metadata.put(Constants.OWNER, userDetails.getId());
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename(name);
        fileMetadata.setOwner(userDetails.getId());
        fileMetadata.setFileId(uploadFile(file, metadata, name, contentType).toString());
        fileRepository.save(fileMetadata);
        return fileMetadata.getFileId();
    }

    @Async
    @CachePut("file")
    public ObjectId uploadFile(InputStream file, DBObject metadata, String name, String contentType) {
        return template.store(file, name, contentType,
                metadata);
    }

    @CacheEvict(value = "files", allEntries = true)
    public List<FileUpload> getMetadataFilesFromUser() {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        GridFSFindIterable gridFSFiles = template.find(new Query(
                GridFsCriteria.whereMetaData(Constants.OWNER)
                        .is(userDetails.getId())));
        List<FileUpload> fileUploads = new ArrayList<>();
        gridFSFiles.forEach(gridFSFile -> {
            FileUpload fileUpload = new FileUpload();
            fileUpload.setId(gridFSFile.getObjectId().toString());
            fileRepository.findByFileId(fileUpload.getId())
                    .ifPresent(file -> fileUpload.setFilename(file.getFilename()));
            fileUpload.setStatus(gridFSFile.getMetadata().get(Constants.STATUS).toString());
            fileUpload.setFileType(gridFSFile.getMetadata().get(Constants.CONTENTTYPE).toString());
            fileUpload.setFileSize(gridFSFile.getMetadata().get(Constants.FILESIZE).toString());
            fileUploads.add(fileUpload);
        });
        FileService.log.info("Displaying list of files for user {}", userDetails.getUsername());
        return fileUploads;
    }

    @CacheEvict(value = "file")
    public FileUpload downloadFile(String id) throws IOException, FileForbiddenException, ResourceNotFoundException {
        UserDetailsImpl userDetails = getUser(id);

        Optional<GridFSFile> gridFSFileOptional = Optional
                .ofNullable(template.findOne(new Query(Criteria.where(Constants.ID).is(id))));
        FileUpload fileUpload = new FileUpload();
        try {
            GridFSFile gridFSFile = gridFSFileOptional.get();
            Document metadata = gridFSFile.getMetadata();

            fileRepository.findByFileId(id).ifPresent(file -> fileUpload.setFilename(file.getFilename()));
            fileUpload.setId(id);
            fileUpload.setFileType(metadata.get(Constants.CONTENTTYPE).toString());
            fileUpload.setStatus(metadata.get(Constants.STATUS).toString());
            fileUpload.setFileSize(metadata.get(Constants.FILESIZE).toString());

            if (userDetails.getId().equals(metadata.get(Constants.OWNER).toString())) {
                fileUpload.setFile(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
            } else {
                FileService.log.error("Attempt to download file {} from user {}",
                        fileUpload.getFilename(), userDetails.getUsername());
                throw new FileForbiddenException(Constants.FORBIDDENFILE + id);
            }
            FileService.log.info("File {} downloaded", fileUpload.getFilename());
        } catch (NoSuchElementException npe) {
            FileService.log.error("File {} not found", id);
            throw new ResourceNotFoundException("File with id " + id + " not found");
        }
        return fileUpload;
    }

    public void updateFile(String name, String id) throws ResourceNotFoundException, FileForbiddenException {
        UserDetailsImpl userDetails = getUser(id);
        Optional<FileMetadata> fileMetadata = fileRepository.findByFileId(id);
        if (!fileMetadata.isPresent()) {
            throw new ResourceNotFoundException(id);
        } else {
            if (userDetails.getId().equals(fileMetadata.get().getOwner())) {
                fileMetadata.get().setFilename(name.concat(".zip"));
                fileRepository.save(fileMetadata.get());
            } else {
                FileService.log.error("Attempt to update file {} from user {}",
                        fileMetadata.get().getFilename(), userDetails.getUsername());
                throw new FileForbiddenException(Constants.FORBIDDENFILE + id);
            }
        }
    }

    @CacheEvict(value = "files")
    public String deleteFile(String id) throws FileForbiddenException, ResourceNotFoundException {

        UserDetailsImpl userDetails = getUser(id);
        Query query = new Query(Criteria.where(Constants.ID).is(id));
        Optional<GridFSFile> gridFSFileOptional = Optional
                .ofNullable(template.findOne(query));
        try {
            GridFSFile gridFSFile = gridFSFileOptional.get();
            if (userDetails.getId().equals(gridFSFile.getMetadata().get(Constants.OWNER).toString())) {
                template.delete(query);
                fileRepository.deleteByFileId(id);
                FileService.log.info("File {} deleted", id);
            } else {
                FileService.log.error("Attempt to delete file {} with id {} from user {}",
                        gridFSFile.getFilename(), gridFSFile.getObjectId().toString(),
                        userDetails.getUsername());
                throw new FileForbiddenException(Constants.FORBIDDENFILE + id);
            }
        } catch (NoSuchElementException npe) {
            FileService.log.error("File {} not found", id);
            throw new ResourceNotFoundException("File with id " + id + " not found");
        }
        return "File with id: " + id + " was deleted successfully";
    }

    public UserDetailsImpl getUser(String id) throws FileForbiddenException {
        try {
            return (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal();
        } catch (NullPointerException npe) {
            throw new FileForbiddenException(Constants.FORBIDDENFILE + id);
        }
    }


}
