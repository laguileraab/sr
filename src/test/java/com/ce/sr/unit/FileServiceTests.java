package com.ce.sr.unit;

import com.ce.sr.exceptions.FileForbiddenException;
import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.models.FileMetadata;
import com.ce.sr.payload.response.FileUpload;
import com.ce.sr.repository.FileRepository;
import com.ce.sr.services.FileService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;

@WithUserDetails("user")
@SpringBootTest
public class FileServiceTests {

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
		fileService.addFile(file, size, "testFile.txt", "text/plain");
	}

	@AfterEach
	public void clean() {
		Query query = new Query(Criteria.where("filename").is(filename));
		GridFSFile doc = template.findOne(query);
		if (doc != null)
			fileRepository.deleteByFileId(doc.getObjectId().toString());
		template.delete(query);
	}

	@Test
	public void uploadFileTest() throws InterruptedException {
		GridFSFile doc = template.findOne(new Query(Criteria.where("filename").is(filename)));
		assertNotNull(doc);
	}

	@Test
	public void listMetadataFilesTest() {

		List<FileUpload> fileUploads = fileService.getMetadataFilesFromUser();
		assertNotNull(fileUploads);
		assertTrue(fileUploads.size() > 0);
		assertNotEquals(0, fileUploads.size());
	}

	@Test
	public void downloadFileTest() throws IOException, FileForbiddenException, ResourceNotFoundException {
		GridFSFile file = template.findOne(new Query(Criteria.where("filename").is(filename)));
		FileUpload fileUpload = fileService.downloadFile(file.getObjectId().toString());
		assertNotNull(fileUpload);
		assertEquals(file.getObjectId().toString(), fileUpload.getId());
	}

	@Test
	public void updateFileTest() throws IOException, FileForbiddenException, ResourceNotFoundException {
		GridFSFile gridFile = template.findOne(new Query(Criteria.where("filename").is(filename)));

		fileService.updateFile(newFilename, gridFile.getObjectId().toString());
		Optional<FileMetadata> file = fileRepository.findByFileId(gridFile.getObjectId().toString());

		assertNotNull(gridFile);
		assertNotNull(file);
		assertEquals(file.get().getFileId(), gridFile.getObjectId().toString());
		assertEquals(newFilename + ".zip", file.get().getFilename());

	}

	@Test
	public void deleteFileTest() throws IOException, FileForbiddenException, ResourceNotFoundException {
		GridFSFile file = template.findOne(new Query(Criteria.where("filename").is("testFile.txt.zip")));
		String response = fileService.deleteFile(file.getObjectId().toString());
		assertNotNull(response);
		assertEquals("File with id: " + file.getObjectId().toString() + " was deleted successfully", response);
	}
}
