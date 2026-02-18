package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class RemoveSpacesRule implements CleaningRule {
    @Override
    public String getType() { return "remove_spaces"; }

    @Override
    public String apply(String value) {
        return (value == null) ? null : value.replace(" ", "");
    }
}