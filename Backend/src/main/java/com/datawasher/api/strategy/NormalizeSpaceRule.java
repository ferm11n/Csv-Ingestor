package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("NORMALIZE_SPACES")
public class NormalizeSpaceRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null) return null;

        return value.trim().replaceAll("\\s+", " ");
    }
}
