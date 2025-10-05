package com.apetitto.apetittoerpbackend.erp.warehouse.model.enums;

import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Getter
public enum UnitType {

    PIECE("шт.", 1),

    GRAM("гр.", 1),
    KILOGRAM("кг.", 1000),

    MILLILITER("мл.", 1),
    LITER("л.", 1000),

    MILLIMETER("мм.", 1),
    METER("м.", 1000);

    private final String displayName;
    private final int conversionFactor; // Коэффициент для пересчета в базовую единицу

    UnitType(String displayName, int conversionFactor) {
        this.displayName = displayName;
        this.conversionFactor = conversionFactor;
    }

    public long toBaseUnit(BigDecimal value) {
        return value.multiply(BigDecimal.valueOf(this.conversionFactor)).longValue();
    }

    public BigDecimal fromBaseUnit(long value) {
        return BigDecimal.valueOf(value)
                .divide(BigDecimal.valueOf(this.conversionFactor), 4, RoundingMode.HALF_UP);
    }
}