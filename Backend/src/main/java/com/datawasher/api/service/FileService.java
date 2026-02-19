package com.datawasher.api.service;

import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.model.FileData;
import com.datawasher.api.model.FileResponse;
import com.datawasher.api.strategy.CleaningRule;
import com.opencsv.CSVReader;
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

    // shared cache of file data – static so all instances see same storage
    private static final Map<String, FileData> fileCache = new ConcurrentHashMap<>();

    private final CsvBusinessService csvBusinessService;
    private final FileStorageService fileStorageService;
    private final Map<String, CleaningRule> strategyMap;

    public FileService(CsvBusinessService csvBusinessService, 
                       FileStorageService fileStorageService,
                       List<CleaningRule> ruleList) { 
        this.csvBusinessService = csvBusinessService;
        this.fileStorageService = fileStorageService;
        this.strategyMap = ruleList.stream()
                .collect(Collectors.toMap(CleaningRule::getType, rule -> rule));
    }

    public FileResponse processFile(MultipartFile file) {
        //validacion csv
        if (file.isEmpty()) {
            throw new RuntimeException("El archivo esta vacio");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.toLowerCase().endsWith(".csv")) {
            throw new RuntimeException("Formato no soportado. Solo se admiten archivos con extension .CSV");
        }

        UploadResponse resp = csvBusinessService.uploadAndPreview(file);
        String cleanId = resp.fileId().replace(".csv", "");

        // headers are returned as array by the upload service; keep them that way in the model
        fileCache.put(cleanId, new FileData(resp.fileName(), resp.headers(), resp.rows()));
        return new FileResponse(cleanId, resp.fileName(), Arrays.asList(resp.headers()), resp.rows());
    }

    public FileResponse cleanFile(String fileId, String ruleType, int colIndex) {
        String cleanId = fileId.replace(".csv", "");
        FileData data = fileCache.get(cleanId);

        if (data == null) {
            data = tryRebuildFromDisk(cleanId);
            if (data != null) fileCache.put(cleanId, data);
        }

        if (data == null) {
            throw new RuntimeException("Archivo no encontrado. Por favor, súbelo de nuevo.");
        }

        List<String[]> currentRows = data.rows();
        List<String[]> cleanedRows = new ArrayList<>();
        String ruleKey = ruleType.toLowerCase();

        if (ruleKey.equals("remove_nulls")) {
             for (String[] row : currentRows) {
                 boolean keepRow = true;
                 
                 if (colIndex >= 0) {
                     if (colIndex < row.length) {
                         String cell = row[colIndex];
                         if (cell == null || cell.trim().isEmpty()) {
                             keepRow = false;
                         }
                     }
                 } 
                 else {
                     boolean isRowEmpty = Arrays.stream(row).allMatch(c -> c == null || c.trim().isEmpty());
                     if (isRowEmpty) keepRow = false;
                 }
                 
                 if (keepRow) cleanedRows.add(row);
             }
        } 
        else if (ruleKey.equals("remove_duplicates")) {
             Set<String> seen = new HashSet<>();
             for (String[] row : currentRows) {
                 if (seen.add(Arrays.toString(row))) cleanedRows.add(row);
             }
        }
        else {
            CleaningRule strategy = strategyMap.get(ruleKey);
            if (strategy == null) throw new RuntimeException("Regla no reconocida: " + ruleType);

            for (String[] row : currentRows) {
                String[] newRow = Arrays.copyOf(row, row.length);
                if (colIndex >= 0 && colIndex < row.length) {
                    newRow[colIndex] = strategy.apply(row[colIndex]);
                } else {
                    for (int i = 0; i < row.length; i++) {
                        newRow[i] = strategy.apply(row[i]);
                    }
                }
                cleanedRows.add(newRow);
            }
        }

        FileData newData = new FileData(data.fileName(), data.headers(), cleanedRows);
        fileCache.put(cleanId, newData);
        
        return new FileResponse(cleanId, data.fileName(), Arrays.asList(data.headers()), cleanedRows);
    }

    public FileResponse resetFile(String fileId) {
        String cleanId = fileId.replace(".csv", "");
        fileCache.remove(cleanId);
        
        FileData originalData = tryRebuildFromDisk(cleanId);

        //VALIDACIÓN DE SEGURIDAD
        if (originalData == null) {
            throw new RuntimeException("No se pudo restaurar el archivo original. ¿Fue eliminado del servidor?");
        }

        fileCache.put(cleanId, originalData);
        return new FileResponse(cleanId, originalData.fileName(), Arrays.asList(originalData.headers()), originalData.rows());
    }

    private FileData tryRebuildFromDisk(String fileId) {
        try {
            Path path = fileStorageService.loadFileAsPath(fileId + ".csv");
            try (CSVReader reader = new CSVReader(new InputStreamReader(Files.newInputStream(path)))) {
                List<String[]> allRows = reader.readAll();
                if (allRows.isEmpty()) return null;
                return new FileData(path.getFileName().toString(), allRows.get(0), allRows.subList(1, allRows.size()));
            }
        } catch (Exception ex) {
            return null;
        }
    }
}