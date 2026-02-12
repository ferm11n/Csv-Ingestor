package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("ONLY_NUMBERS")
public class KeepOnlyNumbersRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        // Reemplaza todo lo que NO sea un número (0-9) por vacío
        return value.replaceAll("[^0-9]", "");
    }
}
