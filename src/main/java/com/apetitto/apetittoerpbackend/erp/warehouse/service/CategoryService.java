package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Category;

import java.util.List;

public interface CategoryService {

    List<CategoryDto> searchCategoriesByName(String name);

    CategoryDto createCategory(CategoryDto categoryDto);

    List<CategoryDto> getAllCategories();

    CategoryDto getCategoryById(Long id);

    CategoryDto updateCategory(CategoryDto categoryDto);

    Category findCategoryEntityById(Long id);

    void deleteCategory(Long id);
}
