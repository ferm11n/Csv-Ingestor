package com.datawasher.api.service;

import com.datawasher.api.dto.CleanRequest;
import com.datawasher.api.dto.UploadResponse;
import com.datawasher.api.strategy.CleaningRule;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.springframework.stereotype.Service;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

@Service
public class CleaningService {

    // Mapa de reglas
    private final Map<String, CleaningRule> strategies;
    private final FileStorageService fileStorageService;
    private final CsvBusinessService csvBusinessService;

    //spring te inyecta (rico) automáticamente todas las reglas en este mapita
    public CleaningService(Map<String, CleaningRule> strategies,
                           FileStorageService fileStorageService,
                           CsvBusinessService csvBusinessService) {
        this.strategies = strategies;
        this.fileStorageService = fileStorageService;
        this.csvBusinessService = csvBusinessService;
    }

    public UploadResponse cleanFile(CleanRequest request) {
        // existe la regla?
        CleaningRule rule = strategies.get(request.ruleName());
        if (rule == null) {
            throw new IllegalArgumentException("Regla no encontrada: " + request.ruleName());
        }

        //buscate el archivo
        File inputFile = fileStorageService.loadFileAsPath(request.fileId()).toFile();
        
        //ponele un nombre al nuevo archivo
        String newFileId = "clean_" + UUID.randomUUID().toString() + ".csv";
        File outputFile = fileStorageService.loadFileAsPath(newFileId).toFile();

        //configuracion del Univocity para leer y escribir al mismo tiempo
        CsvParserSettings parserSettings = new CsvParserSettings();
        parserSettings.setLineSeparatorDetectionEnabled(true);
        parserSettings.setHeaderExtractionEnabled(true);
        parserSettings.setMaxCharsPerColumn(20000);

        CsvWriterSettings writerSettings = new CsvWriterSettings();

        CsvParser parser = new CsvParser(parserSettings);
        CsvWriter writer = new CsvWriter(outputFile, StandardCharsets.UTF_8, writerSettings);

        //PROCESO DE LIMPIEZA (Streaming) y devolucion
        parser.beginParsing(inputFile, StandardCharsets.UTF_8);

        String[] headers = parser.getRecordMetadata().headers();
        if (headers == null) {
            parser.parseNext();
            headers = parser.getContext().headers();
        }
        writer.writeHeaders(headers);

        String[] row;
        while ((row = parser.parseNext()) != null) {
            if (request.columnIndex() < row.length) {
                String originalValue = row[request.columnIndex()];
                row[request.columnIndex()] = rule.apply(originalValue);
            }
            writer.writeRow(row);
        }

        parser.stopParsing();
        writer.close();

        return csvBusinessService.previewExistingFile(newFileId, "clean_" + inputFile.getName());
    }
}
