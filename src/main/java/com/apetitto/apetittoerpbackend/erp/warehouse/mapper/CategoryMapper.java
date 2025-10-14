package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.CategoryDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Category;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    CategoryDto toDto(Category category);

    Category toEntity(CategoryDto categoryDto);

    List<CategoryDto> toDtoList(List<Category> categories);

    void updateEntityFromDto(CategoryDto categoryDto, @MappingTarget Category entity);
}
