package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component("UPPERCASE")
public class UpperCaseRule implements CleaningRule {
    @Override
    public String apply(String value) {
        if (value == null) return null;
        //me gusta todo en mayuscula
        return value.toUpperCase();
    }
}
