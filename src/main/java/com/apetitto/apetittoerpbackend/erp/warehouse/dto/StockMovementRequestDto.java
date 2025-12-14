package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StockMovementRequestDto {
    private Long warehouseId;
    private MovementType movementType;
    private String comment;
    private List<Item> items;
    private Long financeAccountId;

    @Data
    public static class Item {
        private Long productId;
        private BigDecimal quantity;
        private BigDecimal costPrice;
    }
}