package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("MASK_EMAIL")
public class MaskEmailRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        
        // Valida que sea un email básico
        if (!value.contains("@")) {
            return value;
        }
        
        String[] parts = value.split("@");
        if (parts.length != 2) {
            return value;
        }
        
        String localPart = parts[0];
        String domain = parts[1];
        
        // Enmascarar: mostrar primer carácter, luego asteriscos, luego el dominio
        if (localPart.length() > 1) {
            String masked = localPart.charAt(0) + "*".repeat(Math.max(0, localPart.length() - 1));
            return masked + "@" + domain;
        }
        
        return value;
    }
}
