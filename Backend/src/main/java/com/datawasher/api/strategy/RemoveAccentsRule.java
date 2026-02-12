package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;
import java.text.Normalizer;
import java.util.regex.Pattern;

@Component("REMOVE__ACCENTS")
public class RemoveAccentsRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        
        //Separar la letra de la tilde nfd), luego borrar los simbolos
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(normalized).replaceAll("");
    }
}
