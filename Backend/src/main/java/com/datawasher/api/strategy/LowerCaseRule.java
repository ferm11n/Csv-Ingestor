package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class LowerCaseRule implements CleaningRule {
    @Override
    public String getType() {
        return "lowercase";
    }

    @Override
    public String apply(String value) {
        return (value == null) ? null : value.toLowerCase();
    }
}