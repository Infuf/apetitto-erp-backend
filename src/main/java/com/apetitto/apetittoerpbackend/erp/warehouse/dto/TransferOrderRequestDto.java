package com.apetitto.apetittoerpbackend.erp.warehouse.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class TransferOrderRequestDto {
    private Long sourceWarehouseId;
    private Long destinationWarehouseId;
    private List<Item> items;

    @NotNull
    private Boolean isAutoInbound;

    @Data
    public static class Item {
        private Long productId;
        private BigDecimal quantity;
    }
}
