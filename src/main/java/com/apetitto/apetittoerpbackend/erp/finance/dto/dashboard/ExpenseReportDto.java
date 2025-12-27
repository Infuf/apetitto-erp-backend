package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ExpenseReportDto {
    private BigDecimal totalExpense;
    private List<CategoryExpenseDto> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryExpenseDto {
        private String categoryName;
        private BigDecimal amount;
        private BigDecimal percentage;
        private List<SubCategoryExpenseDto> subcategories;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubCategoryExpenseDto {
        private String subCategoryName;
        private BigDecimal amount;
    }
}