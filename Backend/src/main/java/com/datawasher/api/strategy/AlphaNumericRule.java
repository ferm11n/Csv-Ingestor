package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("ALPHA_NUMERIC")
public class AlphaNumericRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;
        // Reemplaza todo lo que NO sea letra (a-z) ni número (0-9)
        return value.replaceAll("[^a-zA-Z0-9]", "");
    }
}
