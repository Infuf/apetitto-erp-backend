package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.ResourceNotFoundException;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;
import com.apetitto.apetittoerpbackend.erp.finance.mapper.FinanceCategoryMapper;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceSubCategory;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.repository.FinanceSubCategoryRepository;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FinanceCategoryServiceImpl implements FinanceCategoryService {

    private final FinanceCategoryRepository categoryRepository;
    private final FinanceSubCategoryRepository subCategoryRepository;
    private final FinanceCategoryMapper categoryMapper;

    @Override
    @Transactional
    public FinanceCategoryDto createCategory(FinanceCategoryDto dto) {
        FinanceCategory category = categoryMapper.toEntity(dto);
        category.setIsActive(true);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public FinanceCategoryDto updateCategory(Long id, FinanceCategoryDto dto) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category NOT found: " + id));

        categoryMapper.updateEntityFromDto(dto, category);
        return categoryMapper.toDto(categoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        FinanceCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category NOT found: " + id));

        category.setIsActive(false);
        if (category.getSubcategories() != null) {
            category.getSubcategories().forEach(sub -> sub.setIsActive(false));
        }
        categoryRepository.save(category);
    }

    @Override
    @Transactional
    public FinanceCategoryDto.FinanceSubCategoryDto createSubCategory(Long categoryId, String name) {
        FinanceCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("Category NOT found: " + categoryId));

        FinanceSubCategory subCategory = new FinanceSubCategory();
        subCategory.setName(name);
        subCategory.setCategory(category);
        subCategory.setIsActive(true);

        return categoryMapper.toSubDto(subCategoryRepository.save(subCategory));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FinanceCategoryDto> getAllCategories() {
        return categoryRepository.findAll().stream()
                .filter(FinanceCategory::getIsActive)
                .map(categoryMapper::toDto)
                .toList();
    }
}