package com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IncomingStockReportDto {
    private String warehouseName;
    private String productName;
    private BigDecimal totalQuantity;
    private BigDecimal pricePerUnit;
    private BigDecimal totalAmount;
}