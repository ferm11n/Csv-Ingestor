package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class UpperCaseRule implements CleaningRule {
    @Override
    public String getType() { return "uppercase"; }

    @Override
    public String apply(String value) {
        return (value == null) ? null : value.toUpperCase();
    }
}