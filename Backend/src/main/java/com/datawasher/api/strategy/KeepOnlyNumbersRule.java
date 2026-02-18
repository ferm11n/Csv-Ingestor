package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class KeepOnlyNumbersRule implements CleaningRule {
    @Override
    public String getType() { return "only_numbers"; }

    @Override
    public String apply(String value) {
        return (value == null) ? null : value.replaceAll("[^0-9]", "");
    }
}