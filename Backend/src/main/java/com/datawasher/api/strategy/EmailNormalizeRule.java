package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class EmailNormalizeRule implements CleaningRule {
    @Override
    public String getType() { return "email_normalize"; }

    @Override
    public String apply(String value) {
        if (value == null) return null;

        String clean = value.trim().toLowerCase();

        clean = clean.replace(" ", "");

        return clean;
    }
}