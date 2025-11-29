package com.apetitto.apetittoerpbackend.erp.finance.mapper;

import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceCategoryDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceCategory;
import com.apetitto.apetittoerpbackend.erp.finance.model.FinanceSubCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface FinanceCategoryMapper {

    FinanceCategoryDto toDto(FinanceCategory category);

    FinanceCategory toEntity(FinanceCategoryDto dto);

    List<FinanceCategoryDto> toDtoList(List<FinanceCategory> categories);

    @Mapping(source = "category.id", target = "categoryId")
    FinanceCategoryDto.FinanceSubCategoryDto toSubDto(FinanceSubCategory subCategory);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDto(FinanceCategoryDto dto, @MappingTarget FinanceCategory entity);
}