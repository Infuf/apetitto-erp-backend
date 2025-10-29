package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockItemMapper {

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.productCode", target = "productCode")
    @Mapping(source = "product.unit", target = "unit")
    StockItemDto toDto(StockItem stockItem);

    List<StockItemDto> toDtoList(List<StockItem> stockItems);

}
