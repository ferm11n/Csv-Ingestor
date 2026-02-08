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

    // Inyección de Dependencia
    public CsvBusinessService(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    public UploadResponse uploadAndPreview(MultipartFile file) {
        //Guardar el archivo físicamente (reutilizamos tu código anterior)
        String fileId = fileStorageService.storeFile(file);
        
        //Obtener el archivo real del disco para poder leerlo
        File fileOnDisk = new File(fileStorageService.loadFileAsPath(fileId).toString());

        //Configurar el lector (Univocity Parsers)
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true); // Detectar si usa saltos de línea de Windows o Linux
        settings.setHeaderExtractionEnabled(true);       // Asumir que la primera fila son títulos
        settings.setMaxCharsPerColumn(20000);            // Seguridad: evitar que una columna gigante bloquee la memoria

        CsvParser parser = new CsvParser(settings);

        //Abrir el archivo y leer (Usamos UTF-8 para acentos y ñ)
        parser.beginParsing(fileOnDisk, StandardCharsets.UTF_8);

        //Extraer cabeceras
        String[] headers = parser.getRecordMetadata().headers();
        
        // Truco de seguridad: 
        // Si el archivo es raro y no detecta headers, forzamos la lectura
        if (headers == null) {
            parser.parseNext(); 
            headers = parser.getContext().headers();
        }

        //leemos solo las primeras 10 filas
        List<String[]> previewRows = new ArrayList<>();
        String[] row;
        int rowCount = 0;

        //"Mientras haya filas Y llevamos menos de 10"
        while ((row = parser.parseNext()) != null && rowCount < 10) {
            previewRows.add(row);
            rowCount++;
        }
        
        // IMPORTANTE: Cerrar el archivo para liberar memoria (como no lo hice antes)
        parser.stopParsing();

        //Empaquetar todo en el DTO y devolverlo
        return new UploadResponse(
            fileId, 
            file.getOriginalFilename(), 
            headers, 
            previewRows
        );
    }

    //Nuevo: Para previsualizar un archivo que YA existe en disco
    public UploadResponse previewExistingFile(String fileId, String fileName) {
        File fileOnDisk = fileStorageService.loadFileAsPath(fileId).toFile();
        
        CsvParserSettings settings = new CsvParserSettings();
        settings.setLineSeparatorDetectionEnabled(true);
        settings.setHeaderExtractionEnabled(true);
        settings.setMaxCharsPerColumn(20000);

        CsvParser parser = new CsvParser(settings);
        parser.beginParsing(fileOnDisk, StandardCharsets.UTF_8);

        String[] headers = parser.getRecordMetadata().headers();
        if (headers == null) {
            parser.parseNext(); 
            headers = parser.getContext().headers();
        }

        List<String[]> previewRows = new ArrayList<>();
        String[] row;
        int rowCount = 0;

        while ((row = parser.parseNext()) != null && rowCount < 10) {
            previewRows.add(row);
            rowCount++;
        }
        parser.stopParsing();

        return new UploadResponse(fileId, fileName, headers, previewRows);
    }
}
