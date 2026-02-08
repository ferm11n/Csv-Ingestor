package com.datawasher.api.controller;

import com.datawasher.api.dto.CleanRequest;
import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.service.CleaningService;
import com.datawasher.api.service.CsvBusinessService;
import com.datawasher.api.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final CsvBusinessService csvBusinessService;
    private final CleaningService cleaningService;
    private final FileStorageService fileStorageService;

    //Inyectamos servicios
    public FileController(CsvBusinessService csvBusinessService, CleaningService cleaningService, FileStorageService fileStorageService) {
        this.csvBusinessService = csvBusinessService;
        this.cleaningService = cleaningService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        UploadResponse response = csvBusinessService.uploadAndPreview(file);
        return ResponseEntity.ok(response);
    }

    // NUEVO ENDPOINT PARA LIMPIAR
    @PostMapping("/clean")
    public ResponseEntity<UploadResponse> cleanColumn(@RequestBody CleanRequest request) {
        UploadResponse response = cleaningService.cleanFile(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        
        Resource resource = fileStorageService.loadFileAsResource(fileName);

        String contentType = "application/octet-stream";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}