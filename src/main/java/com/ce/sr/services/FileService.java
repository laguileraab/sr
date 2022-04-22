package com.ce.sr.services;

import com.ce.sr.exceptions.FileForbiddenException;
import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.payload.response.FileUpload;
import com.ce.sr.utils.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
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

    public String addFile(MultipartFile upload) throws IOException {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        DBObject metadata = new BasicDBObject();
        metadata.put(Constants.FILESIZE, upload.getSize());
        metadata.put(Constants.OWNER, userDetails.getId());
        Object fileID = template.store(upload.getInputStream(), upload.getOriginalFilename(), upload.getContentType(),
                metadata);
        return fileID.toString();
    }

    @Cacheable("files")
    public List<FileUpload> getMetadataFilesFromUser() {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        GridFSFindIterable gridFSFiles = template.find(new Query(
                GridFsCriteria.whereMetaData(Constants.OWNER)
                        .is(userDetails.getId())));
        FileUpload fileUpload = new FileUpload();
        List<FileUpload> fileUploads = new ArrayList<>();
        gridFSFiles.forEach(gridFSFile -> {
            if (gridFSFile != null && gridFSFile.getMetadata() != null) {
                fileUpload.setId(gridFSFile.getObjectId().toString());
                fileUpload.setFilename(gridFSFile.getFilename());
                fileUpload.setFileType(gridFSFile.getMetadata().get(Constants.CONTENTTYPE).toString());
                fileUpload.setFileSize(gridFSFile.getMetadata().get(Constants.FILESIZE).toString());
                fileUploads.add(fileUpload);
            }
        });
        FileService.log.info("Displaying list of files");
        return fileUploads;
    }

    @Cacheable("file")
    public FileUpload downloadFile(String id) throws IOException, FileForbiddenException, ResourceNotFoundException {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<GridFSFile> gridFSFileOptional = Optional
                .ofNullable(template.findOne(new Query(Criteria.where(Constants.ID).is(id))));
        FileUpload fileUpload = new FileUpload();
        try {
            GridFSFile gridFSFile = gridFSFileOptional.get();
            if (gridFSFile.getMetadata() != null) {
                fileUpload.setFilename(gridFSFile.getFilename());
                fileUpload.setFileType(gridFSFile.getMetadata().get(Constants.CONTENTTYPE).toString());
                fileUpload.setFileSize(gridFSFile.getMetadata().get(Constants.FILESIZE).toString());
                if (userDetails.getId().equals(gridFSFile.getMetadata().get(Constants.OWNER).toString())) {
                    fileUpload.setFile(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
                } else {
                    FileService.log.error("Attempt to download file {} from user {}",
                            fileUpload.getFilename(), userDetails.getUsername());
                    throw new FileForbiddenException("You don't have access to this file " + id);
                }
            }
            FileService.log.info("File {} downloaded", fileUpload.getFilename());
        } catch (NoSuchElementException npe) {
            FileService.log.error("File {} not found", id);
            throw new ResourceNotFoundException("File with id " + id + " not found");
        }
        return fileUpload;
    }

    @Cacheable("dFile")
    public String deleteFile(String id) throws FileForbiddenException, ResourceNotFoundException {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        Optional<GridFSFile> gridFSFileOptional = Optional
                .ofNullable(template.findOne(new Query(Criteria.where(Constants.ID).is(id))));
        try {
            GridFSFile gridFSFile = gridFSFileOptional.get();
            if (userDetails.getId().equals(gridFSFile.getMetadata().get(Constants.OWNER).toString())) {
                Query query = new Query(Criteria.where(Constants.ID).is(id));
                template.delete(query);
                FileService.log.info("File {} deleted", id);
            } else {
                FileService.log.error("Attempt to delete file {} with id {} from user {}",
                        gridFSFile.getFilename(), gridFSFile.getObjectId().toString(),
                        userDetails.getUsername());
                throw new FileForbiddenException("You don't have access to this file " + id);
            }
        } catch (NoSuchElementException npe) {
            FileService.log.error("File {} not found", id);
            throw new ResourceNotFoundException("File with id " + id + " not found");
        }
        return "File " + id + " was deleted successfully";
    }

}
