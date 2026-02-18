package com.datawasher.api.strategy;

import org.springframework.stereotype.Component;

@Component
public class MaskEmailRule implements CleaningRule {
    @Override
    public String getType() { return "mask_email"; }

    @Override
    public String apply(String value) {
        if (value == null || !value.contains("@")) return value;
        // Mantiene 1er caracter, oculta el usuario, mantiene dominio
        return value.replaceAll("(^[^@]{1})([^@]+)(@.*)", "$1***$3");
    }
}