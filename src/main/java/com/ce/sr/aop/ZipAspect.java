package com.ce.sr.aop;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.models.FileMetadata;
import com.ce.sr.payload.response.FileUpload;
import com.ce.sr.repository.FileRepository;
import com.ce.sr.utils.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BufferedFileChannelInputStream;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Component;

import lombok.extern.log4j.Log4j2;

@Log4j2
@Aspect
@Component
public class ZipAspect {

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private GridFsOperations operations;

    @Autowired
    private FileRepository fileRepository;

    @Pointcut("execution(public * com.ce.sr.services.FileService.addFile(..))")
    public void addFileMethod() {
    };

    @AfterReturning(pointcut = "addFileMethod()", returning = "id")
    public void zipFile(JoinPoint jp, Object id) throws IllegalStateException, IOException, ResourceNotFoundException {
        // Get uploaded document from the database
        Query query = new Query(Criteria.where(Constants.ID).is(id.toString()));
        Optional<GridFSFile> gridFSFileOptional = Optional
                .ofNullable(template.findOne(query));
        try {
            GridFSFile gridFSFile = gridFSFileOptional.get();
            if (gridFSFile.getMetadata() != null) {
                String fileName = gridFSFile.getFilename().concat(".zip");
                byte[] file = IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream());
                // Delete document to keep only the zip file
                template.delete(query);
                // Make the zip file from previous file
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ZipOutputStream zos = new ZipOutputStream(baos);
                ZipEntry entry = new ZipEntry(fileName);
                entry.setSize(file.length);
                zos.putNextEntry(entry);
                zos.write(file);
                zos.closeEntry();
                zos.close();
                // Upload zip file to database with new metadata
                InputStream is = new ByteArrayInputStream(baos.toByteArray());
                DBObject metadata = new BasicDBObject();
                metadata.put(Constants.FILESIZE, file.length);
                metadata.put(Constants.OWNER, gridFSFile.getMetadata().get(Constants.OWNER));
                metadata.put(Constants.STATUS, Constants.READY);
                fileRepository.findByFileId(id.toString()).ifPresent(fileUpdate -> {
                    fileUpdate.setFilename(fileName);
                    ObjectId objectId = template.store(is, fileName, Constants.CONTENTTYPEZIP, metadata);
                    fileUpdate.setFileId(objectId.toString());
                    fileRepository.save(fileUpdate);
                });
            }
        } catch (NoSuchElementException npe) {
            ZipAspect.log.error("File {} not found", id);
            throw new ResourceNotFoundException("File with id " + id + " not found");
        }
    }
}
