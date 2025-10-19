package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class StockMovementDto {
    private Long id;
    private Long warehouseId;
    private String warehouseName;
    private MovementType movementType;
    private LocalDateTime movementTime;
    private Long createdBy;
    private String comment;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal costPrice;
    }
}