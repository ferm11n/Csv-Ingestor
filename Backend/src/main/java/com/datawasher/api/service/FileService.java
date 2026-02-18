package com.datawasher.api.service;

import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.model.FileResponse;
import com.opencsv.CSVReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class FileService {

    private static final Logger logger = LoggerFactory.getLogger(FileService.class);
    private static final Map<String, FileData> fileCache = new ConcurrentHashMap<>();

    private final CsvBusinessService csvBusinessService;
    private final FileStorageService fileStorageService;

    public FileService(CsvBusinessService csvBusinessService, FileStorageService fileStorageService) {
        this.csvBusinessService = csvBusinessService;
        this.fileStorageService = fileStorageService;
    }

    // Estructura interna para caché
    private record FileData(String fileName, List<String> headers, List<String[]> rows) {}

    public FileResponse processFile(MultipartFile file) {
        // Sube a disco y obtiene preview
        UploadResponse resp = csvBusinessService.uploadAndPreview(file);
        
        List<String> headersList = Arrays.asList(resp.headers());
        
        // Si el ID que viene del otro servicio trae .csv, se lo sacamos
        String cleanId = resp.fileId();
        if (cleanId.endsWith(".csv")) {
            cleanId = cleanId.replace(".csv", "");
        }
        
        // Guarda en memoria RAM usando el ID LIMPIO
        fileCache.put(cleanId, new FileData(resp.fileName(), headersList, resp.rows()));
        
        logger.info("Archivo procesado y cacheado. ID: {}", cleanId);

        // Retornamos el ID limpio al Frontend también
        return new FileResponse(cleanId, resp.fileName(), headersList, resp.rows());
    }

    public FileResponse cleanFile(String fileId, String rule) {
        // Normalización de ID
        if (fileId.endsWith(".csv")) {
            fileId = fileId.replace(".csv", "");
        }

        FileData data = fileCache.get(fileId);

        // Intento de recuperación si la RAM se borró (Reinicio del servidor)
        if (data == null) {
            logger.warn("ID {} no encontrado en RAM. Intentando recuperar del disco...", fileId);
            data = tryRebuildFromDisk(fileId);
            if (data != null) {
                fileCache.put(fileId, data);
            }
        }

        if (data == null) {
            logger.error("Fallo crítico: Archivo {} no encontrado ni en RAM ni en disco.", fileId);
            throw new RuntimeException("Archivo no encontrado. Por favor, súbelo de nuevo.");
        }

        List<String[]> cleanedRows = applyRule(data.rows(), rule);

        // Actualizamos caché y retornamos
        FileData newData = new FileData(data.fileName(), data.headers(), cleanedRows);
        fileCache.put(fileId, newData);
        
        logger.info("Regla '{}' aplicada a archivo {}. Filas resultantes: {}", rule, fileId, cleanedRows.size());

        return new FileResponse(fileId, data.fileName(), data.headers(), cleanedRows);
    }

    private List<String[]> applyRule(List<String[]> rows, String rule) {
        return switch (rule.toLowerCase()) {
            case "remove_nulls" -> rows.stream()
                    .filter(row -> Arrays.stream(row).noneMatch(cell -> cell == null || cell.trim().isEmpty()))
                    .collect(Collectors.toList());
            case "uppercase" -> rows.stream()
                    .map(row -> Arrays.stream(row)
                            .map(cell -> cell != null ? cell.toUpperCase() : "")
                            .toArray(String[]::new))
                    .collect(Collectors.toList());
            case "lowercase" -> rows.stream()
                    .map(row -> Arrays.stream(row)
                            .map(cell -> cell != null ? cell.toLowerCase() : "")
                            .toArray(String[]::new))
                    .collect(Collectors.toList());
            case "trim" -> rows.stream()
                    .map(row -> Arrays.stream(row)
                            .map(cell -> cell != null ? cell.trim() : "")
                            .toArray(String[]::new))
                    .collect(Collectors.toList());
            case "remove_duplicates" -> {
                Set<String> seen = new HashSet<>();
                List<String[]> unique = new ArrayList<>();
                for (String[] row : rows) {
                    String fingerprint = Arrays.toString(row);
                    if (seen.add(fingerprint)) {
                        unique.add(row);
                    }
                }
                yield unique;
            }
            default -> throw new RuntimeException("Regla no reconocida: " + rule);
        };
    }

    private FileData tryRebuildFromDisk(String fileId) {
        try {
            Path path = fileStorageService.loadFileAsPath(fileId);
            try (CSVReader reader = new CSVReader(new InputStreamReader(Files.newInputStream(path)))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) return null;
                
                List<String> headers = Arrays.asList(allRows.get(0));
                List<String[]> rows = allRows.subList(1, allRows.size());
                return new FileData(path.getFileName().toString(), headers, rows);
            }
        } catch (Exception ex) {
            logger.error("Error al reconstruir desde disco: {}", ex.getMessage());
            return null;
        }
    }
}