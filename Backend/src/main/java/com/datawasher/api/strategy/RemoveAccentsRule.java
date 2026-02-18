package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;
import java.text.Normalizer;

@Component
public class RemoveAccentsRule implements CleaningRule {
    @Override
    public String getType() { return "remove_accents"; }

    @Override
    public String apply(String value) {
        if (value == null) return null;
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        // Borra los caracteres diacríticos (tildes, diéresis)
        return normalized.replaceAll("\\p{InCombiningDiacriticalMarks}+", "");
    }
}