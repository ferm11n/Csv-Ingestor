package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class MaskCardRule implements CleaningRule {
    @Override
    public String getType() { return "mask_card"; }

    @Override
    public String apply(String value) {
        if (value == null) return null;
        if (value.length() <= 4) return value;
        
        return "*".repeat(value.length() - 4) + value.substring(value.length() - 4);
    }
}