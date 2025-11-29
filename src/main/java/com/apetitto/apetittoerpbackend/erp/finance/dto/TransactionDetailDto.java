package com.apetitto.apetittoerpbackend.erp.finance.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class TransactionDetailDto extends TransactionResponseDto {

    private List<ItemDetailDto> items;

    @Data
    public static class ItemDetailDto {
        private Long productId;
        private String productName;
        private String productCode;
        private BigDecimal quantity;
        private BigDecimal priceSnapshot;
        private BigDecimal totalAmount;
    }
}