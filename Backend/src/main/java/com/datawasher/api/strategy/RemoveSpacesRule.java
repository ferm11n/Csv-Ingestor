package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("REMOVE_SPACES")
public class RemoveSpacesRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        // Reemplaza cualquier espacio ( ) por nada ("")
        return value.replace(" ", "");
    }
}