package com.datawasher.api.service;

import org.springframework.beans.factory.annotation.Value; // Para leer application.properties
import org.springframework.stereotype.Service; // Para marcarlo como componente de Spring
import org.springframework.util.StringUtils; // Utilidad para limpiar textos
import org.springframework.web.multipart.MultipartFile; // El tipo de dato "Archivo"

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

@Service // [1] Le indicas a Spring que guarde esta clase en memoria
public class FileStorageService {

    // La ruta final donde guardaremos cosas
    private final Path fileStorageLocation;

    // [2] Inyección del valor desde application.properties
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        try {
            // Convierte el texto "uploads" en una Ruta absoluta del sistema operativo
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

            // Crea la carpeta si no existe
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio para subir archivos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // [3] Limpia el nombre del archivo (seguridad)
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Verificación básica de seguridad
            if (originalFileName.contains("..")) {
                throw new RuntimeException("El archivo contiene una ruta inválida: " + originalFileName);
            }

            // [4] Generar nombre único para evitar colisiones
            // Ejemplo: Si subes "data.csv", lo guardamos como "550e8400-e29b...data.csv"
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // [5] Copiar el archivo al destino
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Devolvemos el nombre generado para que el Controller se lo diga al usuario
            return uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Falló al guardar el archivo " + originalFileName, ex);
        }
    }
}