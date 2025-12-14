package com.apetitto.apetittoerpbackend.erp.finance.dto;

import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class FinanceAccountDto {
    private Long id;
    private String name;
    private FinanceAccountType type;
    private BigDecimal balance;
    private String description;
    private Boolean isActive;

    private Long userId;
    private String username;
    private Instant createdAt;
    private BigDecimal discountPercentage;
}
