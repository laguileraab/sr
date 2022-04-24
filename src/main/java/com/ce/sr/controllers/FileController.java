package com.ce.sr.controllers;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Future;

import javax.validation.Valid;

import com.ce.sr.exceptions.FileForbiddenException;
import com.ce.sr.exceptions.ResourceNotFoundException;
import com.ce.sr.payload.response.FileUpload;
import com.ce.sr.payload.response.MessageResponse;
import com.ce.sr.services.FileService;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.AsyncResult;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.extern.log4j.Log4j2;

@Log4j2
@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @GetMapping
    public ResponseEntity<List<FileUpload>> getFiles() {
        FileController.log.info("Showing files to user");
        return ResponseEntity.ok()
                .body(fileService.getMetadataFilesFromUser());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ByteArrayResource> download(@Valid @PathVariable String id)
            throws IOException, FileForbiddenException, ResourceNotFoundException {
        FileController.log.info("Downloading file {}...", id);
        FileUpload fileUpload = fileService.downloadFile(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(fileUpload.getFileType()))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileUpload.getFilename() + "\"")
                .body(new ByteArrayResource(fileUpload.getFile()));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<MessageResponse> upload(@RequestParam("file") MultipartFile file)
            throws IOException {
        FileController.log.info("Uploading file...");
        fileService.addFile(file.getInputStream(), file.getSize(), file.getOriginalFilename(),
                file.getContentType());
        return ResponseEntity
                .ok(new MessageResponse(HttpStatus.CREATED,
                        "File " + file.getOriginalFilename() + " uploaded successfully"));
    }

    @PutMapping("/{id}")
    public ResponseEntity<MessageResponse> update(@RequestParam("name") String name,@Valid @PathVariable String id)
            throws ResourceNotFoundException, FileForbiddenException {
        FileController.log.info("Updating file...");
        fileService.updateFile(name, id);
        return ResponseEntity
                .ok(new MessageResponse(HttpStatus.OK,
                "File " + name + ".zip updated successfully"));
            }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteFile(@Valid @PathVariable String id)
            throws FileForbiddenException, ResourceNotFoundException {
        FileController.log.info("Deleting file {}...", id);
        return ResponseEntity
                .ok(new MessageResponse(HttpStatus.OK, fileService.deleteFile(id)));
    }
}
