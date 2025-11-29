package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Финансы: Категории", description = "Справочник статей доходов и расходов")
@RequestMapping("/api/v1/finance/categories")
public interface FinanceCategoryApi {

    @Operation(summary = "Получить все активные категории")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<FinanceCategoryDto>> getAllCategories();

    @Operation(summary = "Создать категорию")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<FinanceCategoryDto> createCategory(@RequestBody FinanceCategoryDto dto);

    @Operation(summary = "Обновить категорию")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<FinanceCategoryDto> updateCategory(@PathVariable Long id, @RequestBody FinanceCategoryDto dto);

    @Operation(summary = "Архивировать категорию")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<Void> deleteCategory(@PathVariable Long id);

    @Operation(summary = "Создать подкатегорию")
    @PostMapping("/{id}/subcategories")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<FinanceCategoryDto.FinanceSubCategoryDto> createSubCategory(@PathVariable Long id, @RequestParam String name);
}