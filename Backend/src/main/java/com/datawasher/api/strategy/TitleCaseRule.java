package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
public class TitleCaseRule implements CleaningRule {
    @Override
    public String getType() { return "titlecase"; }

    @Override
    public String apply(String value) {
        if (value == null || value.isEmpty()) return value;
        
        return Arrays.stream(value.toLowerCase().split("\\s+"))
                .map(word -> word.isEmpty() ? "" : 
                     Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}