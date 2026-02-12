package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("BOOLEAN_NORMALIZER")
public class BooleanNormalizerRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        
        String normalized = value.trim().toLowerCase();
        
        // Valores que representan TRUE
        if (normalized.matches("^(sí|si|yes|true|verdadero|1|on|activo)$")) {
            return "TRUE";
        }
        
        // Valores que representan FALSE
        if (normalized.matches("^(no|false|falso|0|off|inactivo)$")) {
            return "FALSE";
        }
        
        // Si no coincide con ninguno, devuelve el valor original
        return value;
    }
}
