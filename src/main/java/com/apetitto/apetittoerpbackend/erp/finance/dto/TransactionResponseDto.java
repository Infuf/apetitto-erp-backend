package com.apetitto.apetittoerpbackend.erp.finance.dto;

import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransactionResponseDto {
    private Long id;
    private Instant transactionDate;

    private BigDecimal amount;
    private FinanceOperationType operationType;

    private String status;


    private Long fromAccountId;
    private String fromAccountName;

    private Long toAccountId;
    private String toAccountName;

    private Long categoryId;
    private String categoryName;

    private Long subcategoryId;
    private String subcategoryName;

    private String description;

    private Long createdBy;
    private String createdByName;
}