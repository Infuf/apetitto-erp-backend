package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;


import com.apetitto.apetittoerpbackend.erp.warehouse.dto.ProductDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Product;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ProductMapper {

    @Mapping(source = "category.id", target = "categoryId")
    @Mapping(source = "category.name", target = "categoryName")
    ProductDto toDto(Product product);

    List<ProductDto> toDtoList(List<Product> products);


    @Mapping(target = "category", ignore = true)
    Product toEntity(ProductDto productDto);

    @Mapping(target = "category", ignore = true)
    void updateEntityFromDto(ProductDto dto, @MappingTarget Product entity);
}
