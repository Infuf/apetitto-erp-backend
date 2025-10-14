package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.mapper.CategoryMapper;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Category;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.CategoryRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Override
    public List<CategoryDto> searchCategoriesByName(String name) {
        if (name == null || name.isBlank()) {
            return List.of();
        }
        List<Category> categories = categoryRepository.findAllByNameContainingIgnoreCase(name);

        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional
    public CategoryDto createCategory(CategoryDto categoryDto) {
        if (categoryDto.getId() != null) {
            throw new InvalidRequestException("For create operation ID should not Exists");
        }
        var category = categoryMapper.toEntity(categoryDto);
        var savedCategory = categoryRepository.save(category);
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDto> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        return categoryMapper.toDtoList(categories);
    }

    @Override
    @Transactional
    public CategoryDto getCategoryById(Long id) {
        var category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(CategoryDto categoryDto) {
        var existingCategory = categoryRepository.findById(categoryDto.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + categoryDto.getId()));

        categoryMapper.updateEntityFromDto(categoryDto, existingCategory);

        var updatedCategory = categoryRepository.save(existingCategory);

        return categoryMapper.toDto(updatedCategory);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }

    public Category findCategoryEntityById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() ->
                new ResourceNotFoundException("Category not found with id: " + id));
    }
}
