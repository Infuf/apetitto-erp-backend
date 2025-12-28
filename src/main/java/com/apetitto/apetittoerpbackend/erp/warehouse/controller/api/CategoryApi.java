package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Справочник: Категории", description = "API для управления категориями товаров")
@RequestMapping("/api/v1/categories")
public interface CategoryApi {

    @Operation(summary = "Создание новой категории")
    @ApiResponse(responseCode = "201", description = "Категория успешно создана")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto);

    @Operation(summary = "Поиск категорий по имени", description = "Возвращает список категорий, имя которых содержит поисковую строку")
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_OWNER')")
    ResponseEntity<List<CategoryDto>> searchCategoriesByName(@RequestParam("name") String name);

    @Operation(summary = "Получение списка всех категорий")
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_OWNER')")
    ResponseEntity<List<CategoryDto>> getAllCategories();

    @Operation(summary = "Получение категории по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_OWNER')")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id);

    @Operation(summary = "Обновление существующей категории")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<CategoryDto> updateCategory(@RequestBody CategoryDto categoryDto);

    @Operation(summary = "Удаление категории")
    @ApiResponse(responseCode = "204", description = "Категория успешно удалена")
    @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Void> deleteCategory(@PathVariable Long id);
}
