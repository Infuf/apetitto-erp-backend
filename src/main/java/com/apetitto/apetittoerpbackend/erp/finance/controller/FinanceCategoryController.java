package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.finance.controller.api.FinanceCategoryApi;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class FinanceCategoryController implements FinanceCategoryApi {

    private final FinanceCategoryService categoryService;

    @Override
    public ResponseEntity<List<FinanceCategoryDto>> getAllCategories() {
        return ResponseEntity.ok(categoryService.getAllCategories());
    }

    @Override
    public ResponseEntity<FinanceCategoryDto> createCategory(FinanceCategoryDto dto) {
        return ResponseEntity.ok(categoryService.createCategory(dto));
    }

    @Override
    public ResponseEntity<FinanceCategoryDto> updateCategory(Long id, FinanceCategoryDto dto) {
        return ResponseEntity.ok(categoryService.updateCategory(id, dto));
    }

    @Override
    public ResponseEntity<Void> deleteCategory(Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<FinanceCategoryDto.FinanceSubCategoryDto> createSubCategory(Long id, String name) {
        return ResponseEntity.ok(categoryService.createSubCategory(id, name));
    }
}