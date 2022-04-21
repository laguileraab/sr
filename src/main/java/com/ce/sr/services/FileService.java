package com.ce.sr.services;

import com.ce.sr.models.FileUpload;
import com.ce.sr.utils.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    public List<FileUpload> getMetadataFilesFromUser() {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        GridFSFindIterable gridFSFiles = template.find(new Query(
                Criteria.where(Constants.METADATAOWNER)
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

        return fileUploads;
    }

    public FileUpload downloadFile(String id) throws IOException, FileForbiddenException {
        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();

        GridFSFile gridFSFile = template.findOne(new Query(Criteria.where(Constants.ID).is(id)));

        FileUpload fileUpload = new FileUpload();

        if (gridFSFile.getMetadata() != null) {
            fileUpload.setFilename(gridFSFile.getFilename());

            fileUpload.setFileType(gridFSFile.getMetadata().get(Constants.CONTENTTYPE).toString());

            fileUpload.setFileSize(gridFSFile.getMetadata().get(Constants.FILESIZE).toString());
            if (userDetails.getId().equals(gridFSFile.getMetadata().get(Constants.OWNER).toString())) {
                fileUpload.setFile(IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()));
            } else {
                throw new FileForbiddenException("You don't have access to this file " + id);
            }
        }

        return fileUpload;
    }

    public String deleteFile(String id) throws FileForbiddenException {

        UserDetailsImpl userDetails = (UserDetailsImpl) SecurityContextHolder.getContext().getAuthentication()
                .getPrincipal();
        GridFSFile gridFSFile = template.findOne(new Query(Criteria.where(Constants.ID).is(id)));

        if (userDetails.getId().equals(gridFSFile.getMetadata().get(Constants.OWNER).toString())) {
            template.delete(new Query(Criteria.where(Constants.ID).is(userDetails.getId())));
        } else {
            throw new FileForbiddenException("You don't have access to this file " + id);
        }
        return "File " + id + " was deleted successfully";
    }

}
