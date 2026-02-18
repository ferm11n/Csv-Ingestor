package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class NormalizeSpaceRule implements CleaningRule {
    @Override
    public String getType() { return "normalize_spaces"; }

    @Override
    public String apply(String value) {
        // Reemplaza cualquier secuencia de espacios (\s+) por uno solo
        return (value == null) ? null : value.trim().replaceAll("\\s+", " ");
    }
}