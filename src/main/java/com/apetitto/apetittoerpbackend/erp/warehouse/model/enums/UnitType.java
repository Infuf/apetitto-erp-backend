package com.apetitto.apetittoerpbackend.erp.warehouse.model.enums;

import lombok.Getter;

@Getter
public enum UnitType {

    PIECE("шт."),

    KILOGRAM("кг."),

    LITER("л."),

    METER("м.");

    private final String displayName;

    UnitType(String displayName) {
        this.displayName = displayName;
    }
}