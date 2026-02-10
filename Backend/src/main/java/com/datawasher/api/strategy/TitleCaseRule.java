package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("TITLE_CASE")
public class TitleCaseRule implements CleaningRule {

    @Override
    public String apply(String value) {
        if (value == null || value.isEmpty()) return null;

        String[] words = value.toLowerCase().split("\\s+");
        StringBuilder result = new StringBuilder();

        for (String word : words) {
            if (!word.isEmpty()) {
                // Pone la primera letra en mayúscula y el resto queda en minúscula
                result.append(Character.toUpperCase(word.charAt(0)))
                      .append(word.substring(1))
                      .append(" ");
            }
        }
        return result.toString().trim();
    }
}