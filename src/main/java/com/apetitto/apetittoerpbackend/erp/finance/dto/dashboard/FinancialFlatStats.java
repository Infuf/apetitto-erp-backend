package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class FinancialFlatStats {
    private FinanceOperationType operationType;
    private String toAccountName;
    private String fromAccountName;
    private String categoryName;
    private String subCategoryName;
    private BigDecimal amount;
}