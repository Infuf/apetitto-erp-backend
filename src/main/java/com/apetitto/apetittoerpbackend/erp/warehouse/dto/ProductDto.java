package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.UnitType;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductDto {
    private Long id;
    private String productCode;
    private String name;
    private String description;
    private UnitType unit;
    private String barcode;
    private BigDecimal sellingPrice;

    private Long categoryId;
    private String categoryName;
}
