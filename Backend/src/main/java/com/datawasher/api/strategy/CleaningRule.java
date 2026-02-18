package com.datawasher.api.strategy;

public interface CleaningRule {
    String getType();
    
    String apply(String value); 
}