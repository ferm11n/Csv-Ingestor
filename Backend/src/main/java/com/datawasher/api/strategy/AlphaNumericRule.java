package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class AlphaNumericRule implements CleaningRule {
    @Override
    public String getType() { return "alphanumeric"; }

    @Override
    public String apply(String value) {
        // Reemplaza todo lo que NO (^) sea a-z, A-Z, 0-9 o espacio
        return (value == null) ? null : value.replaceAll("[^a-zA-Z0-9 ]", "");
    }
}