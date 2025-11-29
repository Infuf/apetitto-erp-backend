package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;

import java.util.List;

public interface FinanceCategoryService {
    FinanceCategoryDto createCategory(FinanceCategoryDto dto);

    FinanceCategoryDto updateCategory(Long id, FinanceCategoryDto dto);

    void deleteCategory(Long id);

    FinanceCategoryDto.FinanceSubCategoryDto createSubCategory(Long categoryId, String name);

    List<FinanceCategoryDto> getAllCategories();
}