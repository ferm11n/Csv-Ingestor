package com.datawasher.api.controller;

import com.datawasher.api.model.FileResponse;
import com.datawasher.api.service.FileService;
import com.datawasher.api.service.CleaningService;
import com.datawasher.api.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
// 1. EL IMPORT NUEVO
import org.springframework.test.context.bean.override.mockito.MockitoBean; 
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@WebMvcTest(FileController.class) 
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc; 

    // 2. USAMOS LA NUEVA ANOTACIÓN
    @MockitoBean
    private FileService fileService; 

    @MockitoBean
    private CleaningService cleaningService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("POST /upload - Debe retornar 200 OK si el archivo es válido")
    void testUploadFileSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", 
                "datos.csv", 
                "text/csv", 
                "nombre,email\njuan,juan@test.com".getBytes()
        );

        FileResponse mockResponse = new FileResponse("file1", "datos.csv", List.of("h1"), new ArrayList<>());
        when(fileService.processFile(any())).thenReturn(mockResponse);

        mockMvc.perform(multipart("/api/files/upload").file(file))
                .andExpect(status().isOk()) 
                .andExpect(jsonPath("$.fileName").value("datos.csv")); 
    }

    @Test
    @DisplayName("POST /clean - Debe retornar 200 OK al limpiar")
    void testCleanFileSuccess() throws Exception {
        FileResponse mockResponse = new FileResponse("file1", "datos.csv", List.of("h1"), new ArrayList<>());
        
        when(fileService.cleanFile(anyString(), anyString(), anyInt()))
                .thenReturn(mockResponse);

        mockMvc.perform(post("/api/files/file1.csv/clean")
                        .param("rule", "remove_nulls")
                        .param("colIndex", "-1"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Debe retornar 400 Bad Request si subimos un PDF (Validación del Controller)")
    void testUploadInvalidExtension() throws Exception {
        MockMultipartFile pdfFile = new MockMultipartFile(
                "file", 
                "documento.pdf", 
                "application/pdf", 
                "fake content".getBytes()
        );
        
        when(fileService.processFile(any()))
                .thenThrow(new RuntimeException("Formato no soportado"));

        mockMvc.perform(multipart("/api/files/upload").file(pdfFile))
                .andExpect(status().isBadRequest()) 
                .andExpect(jsonPath("$.message").exists()); 
    }
}