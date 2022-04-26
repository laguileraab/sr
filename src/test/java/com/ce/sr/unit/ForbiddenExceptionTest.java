package com.ce.sr.unit;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.nio.charset.Charset;
import java.io.ByteArrayInputStream;

import com.ce.sr.exceptions.FileForbiddenException;
import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.models.FileMetadata;
import com.ce.sr.repository.FileRepository;
import com.ce.sr.services.FileService;
import com.ce.sr.utils.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.client.gridfs.model.GridFSFile;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.security.test.context.support.WithUserDetails;

@SpringBootTest
public class ForbiddenExceptionTest {

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private GridFsTemplate template;

    @Autowired
    private FileService fileService;

    private String filename = "testFile.txt.zip";
    private String newFilename = "newTestFile.txt";

    @BeforeEach
    public void setup() {
        String text = "This is a test";
        byte[] textBytes = text.getBytes(Charset.forName("UTF-8"));
        Long size = (long) textBytes.length;
        InputStream file = new ByteArrayInputStream(textBytes);

        DBObject metadata = new BasicDBObject();
        metadata.put(Constants.FILESIZE, size);
        metadata.put(Constants.STATUS, Constants.UPLOAD);
        metadata.put(Constants.OWNER, "testId");
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setFilename(filename);
        fileMetadata.setOwner("testId");
        fileMetadata.setFileId(template.store(file, filename, "text/plain", metadata).toString());
        fileRepository.save(fileMetadata);

    }

    @AfterEach
    public void clean() {
        GridFSFile doc = template.findOne(new Query(Criteria.where("filename").is(filename)));
        if (doc != null) {
            doc = template.findOne(new Query(Criteria.where("filename").is(newFilename + ".zip")));
        }
        template.delete(new Query(Criteria.where("filename").is(filename)));
        if (doc != null)
            fileRepository.deleteByFileId(doc.getObjectId().toString());
    }

    @Test
    public void downloadFileWithoutAuthenticationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.downloadFile(file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void downloadFileWithoutAuthorizationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.downloadFile(file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void downloadFileResourceNotFoundTest() {
        assertThrows(ResourceNotFoundException.class, () -> fileService.downloadFile("fakeId"));
    }

    @Test
    public void updateFileWithoutAuthenticationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.updateFile(newFilename,file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void updateFileWithoutAuthorizationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.updateFile(newFilename,file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void updateFileResourceNotFoundTest() {
        assertThrows(ResourceNotFoundException.class, () -> fileService.updateFile("fakeName","fakeId"));
    }

    @Test
    public void deleteFileWithoutAuthenticationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.deleteFile(file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void deleteFileWithoutAuthorizationTest() {
        GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
        assertThrows(FileForbiddenException.class, () -> fileService.deleteFile(file.getObjectId().toString()));
    }

    @WithUserDetails("user")
    @Test
    public void deleteFileResourceNotFoundTest() {
        assertThrows(ResourceNotFoundException.class, () -> fileService.deleteFile("fakeId"));
    }
}
