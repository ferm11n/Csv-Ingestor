package com.datawasher.api.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service; 
import org.springframework.util.StringUtils; 
import org.springframework.web.multipart.MultipartFile; 

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Objects;
import java.util.UUID;

//Le indicas a Spring que guarde esta clase en memoria
@Service
public class FileStorageService {

    // La ruta final donde guardaremos cosas
    private final Path fileStorageLocation;

    //Inyección del valor desde application.properties
    public FileStorageService(@Value("${file.upload-dir}") String uploadDir) {
        try {
            //Convertir el texto "uploads" en una Ruta absoluta del sistema operativo
            this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio para subir archivos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        //Limpiar el nombre del archivo (seguridad)
        String originalFileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));

        try {
            // Verificación básica de seguridad
            if (originalFileName.contains("..")) {
                throw new RuntimeException("El archivo contiene una ruta inválida: " + originalFileName);
            }

            //Generar nombre único para evitar colisiones
            String fileExtension = "";
            int i = originalFileName.lastIndexOf('.');
            if (i > 0) {
                fileExtension = originalFileName.substring(i);
            }
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            //Copiar el archivo al destino (muy importante)
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            // Devolvemos el nombre generado para que el Controller se lo diga al usuario
            return uniqueFileName;

        } catch (IOException ex) {
            throw new RuntimeException("Falló al guardar el archivo " + originalFileName, ex);
        }
    }

    public Path loadFileAsPath(String fileId) {
        return this.fileStorageLocation.resolve(fileId).normalize();
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(Objects.requireNonNull(filePath.toUri()));

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException("Archivo no encontrado: " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new RuntimeException("Archivo no encontrado: " + fileName, ex);
        }
    }
}