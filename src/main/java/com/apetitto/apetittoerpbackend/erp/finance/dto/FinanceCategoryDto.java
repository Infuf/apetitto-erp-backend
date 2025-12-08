package com.apetitto.apetittoerpbackend.erp.finance.dto;

import lombok.Data;

import java.util.List;

@Data
public class FinanceCategoryDto {
    private Long id;
    private String name;
    private String type;
    private Boolean isActive;
    private String description;
    private List<FinanceSubCategoryDto> subcategories;

    @Data
    public static class FinanceSubCategoryDto {
        private Long id;
        private Long categoryId;
        private String name;
        private Boolean isActive;
    }
}