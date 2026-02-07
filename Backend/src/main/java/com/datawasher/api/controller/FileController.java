package com.datawasher.api.controller;

import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.service.CsvBusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Convertis la clase en una API que responde JSON, no HTML
@RestController
// Todas las rutas empezarán con esto
@RequestMapping("/api/files")
public class FileController {

    private final CsvBusinessService csvBusinessService;

    // CAMBIO CLAVE: Ahora inyectamos CsvBusinessService, no FileStorageService
    public FileController(CsvBusinessService csvBusinessService) {
        this.csvBusinessService = csvBusinessService;
    }

    // Aca solo aceptamos peticiones POST, estamos enviando archivos/datos
    @PostMapping("/upload")
    public ResponseEntity<UploadResponse> uploadFile(@RequestParam("file") MultipartFile file) {
        
        // Llamamos a la lógica nueva
        UploadResponse response = csvBusinessService.uploadAndPreview(file);
        
        // Devolvemos un JSON (status 200 OK)
        return ResponseEntity.ok(response);
    }
}