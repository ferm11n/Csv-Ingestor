package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("TRIM")
public class TrimRule implements CleaningRule {
    @Override
    public String apply(String value) {
        if (value == null) return null;
        //elimina el espacio al principio y al final, maribeeeeel!
        return value.trim();
    }
}
