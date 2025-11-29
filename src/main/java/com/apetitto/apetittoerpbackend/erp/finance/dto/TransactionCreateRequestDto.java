package com.apetitto.apetittoerpbackend.erp.finance.dto;

import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
public class TransactionCreateRequestDto {

    @NotNull(message = "Сумма обязательна")
    @Min(value = 0, message = "Сумма должна быть положительной")
    private BigDecimal amount;

    @NotNull(message = "Тип операции обязателен")
    private FinanceOperationType operationType;

    private Long fromAccountId;
    private Long toAccountId;

    private Long categoryId;
    private Long subcategoryId;

    private String description;

    private Instant transactionDate;

    private List<TransactionItemDto> items;

    @Data
    public static class TransactionItemDto {

        @NotNull
        private Long productId;
        @NotNull
        private BigDecimal quantity;
        @NotNull
        private BigDecimal priceSnapshot;
    }
}