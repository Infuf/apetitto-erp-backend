package com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStockItemDto {
    private Long warehouseId;
    private String warehouseName;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal calculatedValue;
    private String valuationType;
}