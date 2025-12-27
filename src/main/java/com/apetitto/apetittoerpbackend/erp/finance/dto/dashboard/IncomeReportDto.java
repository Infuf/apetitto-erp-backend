package com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class IncomeReportDto {
    private BigDecimal totalIncome;
    private List<CategoryIncomeDto> categories;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class CategoryIncomeDto {
        private String categoryName;
        private BigDecimal amount;
        private BigDecimal percentage;
        private List<SubCategoryIncomeDto> subcategories;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SubCategoryIncomeDto {
        private String subCategoryName;
        private BigDecimal amount;
    }
}