package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Справочник: Категории", description = "API для управления категориями товаров")
@RequestMapping("/api/v1/categories")
public interface CategoryApi {

    @Operation(summary = "Создание новой категории")
    @ApiResponse(responseCode = "201", description = "Категория успешно создана")
    @ApiResponse(responseCode = "400", description = "Некорректный запрос", content = @Content)
    @PostMapping
    ResponseEntity<CategoryDto> createCategory(@RequestBody CategoryDto categoryDto);

    @Operation(summary = "Поиск категорий по имени", description = "Возвращает список категорий, имя которых содержит поисковую строку")
    @GetMapping("/search")
    ResponseEntity<List<CategoryDto>> searchCategoriesByName(@RequestParam("name") String name);

    @Operation(summary = "Получение списка всех категорий")
    @GetMapping
    ResponseEntity<List<CategoryDto>> getAllCategories();

    @Operation(summary = "Получение категории по ID")
    @GetMapping("/{id}")
    ResponseEntity<CategoryDto> getCategoryById(@PathVariable Long id);

    @Operation(summary = "Обновление существующей категории")
    @PutMapping
    ResponseEntity<CategoryDto> updateCategory(@RequestBody CategoryDto categoryDto);

    @Operation(summary = "Удаление категории")
    @ApiResponse(responseCode = "204", description = "Категория успешно удалена")
    @ApiResponse(responseCode = "404", description = "Категория не найдена", content = @Content)
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteCategory(@PathVariable Long id);
}
