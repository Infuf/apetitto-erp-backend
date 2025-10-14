package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.UnitType;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class StockItemDto {
    private Long productId;
    private String productName;
    private String productCode;
    private BigDecimal quantity;
    private UnitType unit;
    private BigDecimal averageCost;
}