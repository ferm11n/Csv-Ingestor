package com.datawasher.api.service;

import com.datawasher.api.dto.UploadResponse;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@Service
public class CsvBusinessService {

    private final FileStorageService fileStorageService;

    // Inyección de Dependencia: Traemos al servicio que ya creaste en la Misión 1
    public CsvBusinessService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public UploadResponse uploadAndPreview(MultipartFile file) {
        // PASO 1: Guardar el archivo físicamente (reutilizamos tu código anterior)
        String fileId = fileStorageService.storeFile(file);
        
        // PASO 2: Obtener el archivo real del disco para poder leerlo
        File fileOnDisk = new File(fileStorageService.loadFileAsPath(fileId).toString());

        // PASO 3: Configurar el lector (Univocity Parsers)
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true); // Detectar si usa saltos de línea de Windows o Linux
        settings.setHeaderExtractionEnabled(true);       // Asumir que la primera fila son títulos
        settings.setMaxCharsPerColumn(20000);            // Seguridad: evitar que una columna gigante bloquee la memoria

        CsvParser parser = new CsvParser(settings);

        // PASO 4: Abrir el archivo y leer (Usamos UTF-8 para acentos y ñ)
        parser.beginParsing(fileOnDisk, StandardCharsets.UTF_8);

        // PASO 5: Extraer las cabeceras
        String[] headers = parser.getRecordMetadata().headers();
        
        // Truco de seguridad: Si el archivo es raro y no detecta headers, forzamos la lectura
        if (headers == null) {
            parser.parseNext(); 
            headers = parser.getContext().headers();
        }

        // PASO 6: Leer solo las primeras 10 filas (Loop while)
        List<String[]> previewRows = new ArrayList<>();
        String[] row;
        int rowCount = 0;

        // "Mientras haya filas Y llevamos menos de 10"
        while ((row = parser.parseNext()) != null && rowCount < 10) {
            previewRows.add(row);
            rowCount++;
        }
        
        parser.stopParsing(); // IMPORTANTE: Cerrar el archivo para liberar memoria

        // PASO 7: Empaquetar todo en el DTO y devolverlo
        return new UploadResponse(
            fileId, 
            file.getOriginalFilename(), 
            headers, 
            previewRows
        );
    }
}
