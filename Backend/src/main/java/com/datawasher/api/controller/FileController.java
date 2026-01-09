package com.datawasher.api.controller;

import com.datawasher.api.service.FileStorageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

// Convertis la clase en una API que responde JSON, no HTML
@RestController
// Todas las rutas empezarán con esto
@RequestMapping("/api/files")
public class FileController {

    private final FileStorageService fileStorageService;

    // [3] Inyección de Dependencia por Constructor
    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    // Aca solo aceptamos peticiones POST, estamos enviando archivos/datos
    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file) { // [5]
        
        // Llamamos al nene para que labure
        String fileName = fileStorageService.storeFile(file);

        // Devolvemos respuesta 200 OK
        return ResponseEntity.ok("Archivo subido con éxito. Guardado como: " + fileName);
    }
}