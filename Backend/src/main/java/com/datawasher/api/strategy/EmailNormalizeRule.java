package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("EMAIL_NORMALIZE")
public class EmailNormalizeRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        // eliminar cualquier whitespace (espacios, tabs, NBSP, newlines) y convertir a minúsculas
        return value.trim().replaceAll("\\s+", "").toLowerCase();
    }
}