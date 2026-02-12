package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("MASK_CARD")
public class MaskCardRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        
        // Extrae solo los dígitos
        String digitsOnly = value.replaceAll("[^0-9]", "");
        
        // Si no tiene al menos 4 dígitos, devolverlo sin cambios
        if (digitsOnly.length() < 4) {
            return value;
        }
        
        // Obtener los últimos 4 dígitos
        String lastFour = digitsOnly.substring(digitsOnly.length() - 4);
        
        // Enmascarar: mostrar últimos 4 dígitos, resto con asteriscos
        int maskLength = digitsOnly.length() - 4;
        String masked = "*".repeat(maskLength) + lastFour;
        
        // Formatear en grupos de 4 dígitos separados por espacios
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < masked.length(); i++) {
            if (i > 0 && i % 4 == 0) {
                result.append(" ");
            }
            result.append(masked.charAt(i));
        }
        
        return result.toString();
    }
}
