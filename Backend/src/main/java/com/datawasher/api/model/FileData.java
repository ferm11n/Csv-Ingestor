package com.datawasher.api.model;

import java.util.List;

// Esto crea automáticamente constructor, getters, equals y hashcode
public record FileData(
    String fileName, 
    String[] headers, 
    List<String[]> rows
) {}