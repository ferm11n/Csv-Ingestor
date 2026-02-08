package com.datawasher.api.dto;

public record CleanRequest(
    // id del archivo a limpiar
    String fileId,
    //el numero de columna
    int columnIndex,
    // el nombre de la regla (trim, uppercase, etc)
    String ruleName
) {}
