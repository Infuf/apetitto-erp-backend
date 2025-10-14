package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.TransferStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class TransferOrderDto {
    private Long id;
    private Long sourceWarehouseId;
    private String sourceWarehouseName;
    private Long destinationWarehouseId;
    private String destinationWarehouseName;
    private TransferStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime shippedAt;
    private LocalDateTime receivedAt;
    private List<Item> items;

    @Data
    public static class Item {
        private Long productId;
        private String productName;
        private BigDecimal quantity;
        private BigDecimal costAtTransfer;
    }
}
