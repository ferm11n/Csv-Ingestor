package com.datawasher.api.controller;

import com.datawasher.api.dto.CleanRequest;
import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.service.CleaningService;
import com.datawasher.api.service.CsvBusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final CsvBusinessService csvBusinessService;
    private final CleaningService cleaningService;

    //Inyectamos servicios
    public FileController(CsvBusinessService csvBusinessService, CleaningService cleaningService) {
        this.csvBusinessService = csvBusinessService;
        this.cleaningService = cleaningService;
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
}