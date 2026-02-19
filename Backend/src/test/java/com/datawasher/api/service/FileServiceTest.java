package com.datawasher.api.service;

import com.datawasher.api.model.FileData;
import com.datawasher.api.model.FileResponse;
// ⚠️ IMPORTANTE: Asegúrate de importar tu interfaz CleaningRule
// (Si está en otro paquete, ajusta esta línea)
import com.datawasher.api.strategy.CleaningRule; 

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private CsvBusinessService csvBusinessService;

    private FileService fileService; 

    private Map<String, FileData> fileCache;

    @SuppressWarnings("null")
    @BeforeEach
    void setUp() {
        List<CleaningRule> emptyRules = new ArrayList<>();

        
        fileService = new FileService(csvBusinessService, null, emptyRules);

        // the cache is a private static final field in FileService; we cannot replace it
        // via ReflectionTestUtils because it's final. Instead grab the existing map and clear it
        // before each test so we start with a fresh state.
        //noinspection unchecked
        fileCache = (Map<String, FileData>) ReflectionTestUtils.getField(FileService.class, "fileCache");
        if (fileCache == null) {
            fileCache = new ConcurrentHashMap<>();
            ReflectionTestUtils.setField(FileService.class, "fileCache", fileCache);
        } else {
            fileCache.clear();
        }

       
        Map<String, CleaningRule> manualStrategyMap = new HashMap<>();
        
        manualStrategyMap.put("uppercase", new CleaningRule() {
            @Override
            public String apply(String input) {
                return (input == null) ? null : input.toUpperCase();
            }

            @Override
            public String getType() {
                return "uppercase";
            }
        });

        ReflectionTestUtils.setField(fileService, "strategyMap", manualStrategyMap);
    }

    // --- HELPER ---
    private void seedCache(String fileId, List<String[]> rows) {
        String[] headers = {"Nombre", "Email", "Edad"};
        FileData data = new FileData("test.csv", headers, rows);
        fileCache.put(fileId, data);
    }

    // --- TESTS  ---

    @Test
    @DisplayName("Debe borrar nulos SOLO de la columna seleccionada")
    void testRemoveNullsSpecificColumn() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Juan", "juan@test.com", "25"});
        rows.add(new String[]{"Pedro", "", "30"});         
        rows.add(new String[]{"", "maria@test.com", "28"}); 

        seedCache("file1", rows);

        FileResponse response = fileService.cleanFile("file1.csv", "remove_nulls", 1);

        assertEquals(2, response.getRows().size());
        assertEquals("Juan", response.getRows().get(0)[0]);
        assertEquals("", response.getRows().get(1)[0]); 
    }

    @Test
    @DisplayName("Debe borrar la fila solo si está COMPLETAMENTE vacía (Global)")
    void testRemoveNullsGlobal() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Juan", "juan@test.com", "25"});
        rows.add(new String[]{"Pedro", "", "30"});          
        rows.add(new String[]{"", "", ""});                 

        seedCache("file2", rows);

        FileResponse response = fileService.cleanFile("file2.csv", "remove_nulls", -1);

        assertEquals(2, response.getRows().size());
        assertEquals("Pedro", response.getRows().get(1)[0]);
    }

    @Test
    @DisplayName("Debe borrar filas duplicadas exactas")
    void testRemoveDuplicates() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"Juan", "juan@test.com"});
        rows.add(new String[]{"Juan", "juan@test.com"}); 
        rows.add(new String[]{"Ana", "ana@test.com"});

        seedCache("file3", rows);

        FileResponse response = fileService.cleanFile("file3.csv", "remove_duplicates", -1);

        assertEquals(2, response.getRows().size());
    }

    @Test
    @DisplayName("Debe transformar texto a Mayúsculas")
    void testUppercaseRule() {
        List<String[]> rows = new ArrayList<>();
        rows.add(new String[]{"juan", "juan@test.com"});

        seedCache("file4", rows);

        FileResponse response = fileService.cleanFile("file4.csv", "uppercase", 0);

        assertEquals("JUAN", response.getRows().get(0)[0]);
    }
    
    @Test
    @DisplayName("Debe lanzar excepción si no existe el archivo")
    void testFileNotFound() {
        Exception exception = assertThrows(RuntimeException.class, () -> {
            fileService.cleanFile("fantasma.csv", "remove_nulls", -1);
        });

        assertTrue(exception.getMessage().contains("Archivo no encontrado"));
    }
}