package com.datawasher.api.dto;

import java.util.List;

/**
 * RECORD (Java 21):
 * Es una clase inmutable de solo datos. Java escribe automáticamente
 * el constructor, los getters y el método toString por nosotros.
 */
public record UploadResponse(
    String fileId,           // El nombre UUID (ej: "a1b2-c3d4.csv") para pedir el archivo luego
    String fileName,         // El nombre original (ej: "clientes_sucios.csv")
    String[] headers,        // La primera fila (ej: ["Nombre", "Email", "Edad"])
    List<String[]> rows      // Las primeras 10 filas de datos para la previsualización
) {}
