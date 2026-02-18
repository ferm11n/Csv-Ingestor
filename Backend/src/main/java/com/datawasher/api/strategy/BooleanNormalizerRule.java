package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class BooleanNormalizerRule implements CleaningRule {
    @Override
    public String getType() { return "boolean_normalize"; }

    @Override
    public String apply(String value) {
        if (value == null) return "false";
        String normalized = value.trim().toLowerCase();
        boolean isTrue = normalized.equals("si") || 
                         normalized.equals("yes") || 
                         normalized.equals("1") || 
                         normalized.equals("true");
        return String.valueOf(isTrue);
    }
}