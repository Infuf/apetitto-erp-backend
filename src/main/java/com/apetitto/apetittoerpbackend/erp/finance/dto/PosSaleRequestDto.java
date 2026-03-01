package com.apetitto.apetittoerpbackend.erp.finance.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
@Data
public class PosSaleRequestDto {
    @NotNull(message = "Сумма обязательна")
    @DecimalMin(value = "0.01", message = "Сумма должно быть больше 0")
    private BigDecimal amount;

    private Long toAccountId;

    private Long categoryId;
    private Long subcategoryId;
    private Long warehouseId;

    private String description;

    private Instant transactionDate;

    private List<TransactionCreateRequestDto.TransactionItemDto> items;
}
