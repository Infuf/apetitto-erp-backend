package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CompanyFinancialStateDto {

    private MoneyState money;

    private DebtState receivables;

    private DebtState payables;

    private BigDecimal netBalance;

    @Data
    @Builder
    public static class MoneyState {
        private BigDecimal totalAmount;
        private List<AccountSummary> details;
    }

    @Data
    @Builder
    public static class DebtState {
        private BigDecimal totalAmount;
        private List<AccountSummary> topDebtors;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AccountSummary {
        private Long id;
        private String name;
        private BigDecimal amount;
    }
}