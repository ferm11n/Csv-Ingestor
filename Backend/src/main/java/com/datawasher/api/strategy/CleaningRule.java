package com.datawasher.api.strategy;

public interface CleaningRule {
    // entra sucio, sale limpio xdddd
    String apply(String value);
}
