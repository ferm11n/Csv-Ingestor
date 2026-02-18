package com.datawasher.api.controller;

import com.datawasher.api.dto.CleanRequest;
import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.model.FileResponse;
import com.datawasher.api.service.CleaningService;
import com.datawasher.api.service.CsvBusinessService;
import com.datawasher.api.service.FileService;
import com.datawasher.api.service.FileStorageService;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
@CrossOrigin(origins = "http://localhost:4200/")
public class FileController {

    private final CsvBusinessService csvBusinessService;
    private final CleaningService cleaningService;
    private final FileStorageService fileStorageService;
    private final FileService fileService;

    // Inyección por constructor (Best Practice)
    public FileController(CsvBusinessService csvBusinessService,
                          CleaningService cleaningService,
                          FileStorageService fileStorageService,
                          FileService fileService) {
        this.csvBusinessService = csvBusinessService;
        this.cleaningService = cleaningService;
        this.fileStorageService = fileStorageService;
        this.fileService = fileService;
    }

    @PostMapping("/upload")
    public ResponseEntity<FileResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        // Usamos fileService para asegurar que se guarde en la caché de memoria
        FileResponse response = fileService.processFile(file);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/clean")
    public ResponseEntity<FileResponse> cleanFile(@PathVariable String id, @RequestParam String rule) {
        FileResponse response = fileService.cleanFile(id, rule);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/download/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName) {
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/octet-stream"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // Endpoints Legacy (Mantener solo si son necesarios para otra funcionalidad)
    @PostMapping("/clean-legacy")
    public ResponseEntity<UploadResponse> cleanColumn(@RequestBody CleanRequest request) {
        UploadResponse response = cleaningService.cleanFile(request);
        return ResponseEntity.ok(response);
    }
}