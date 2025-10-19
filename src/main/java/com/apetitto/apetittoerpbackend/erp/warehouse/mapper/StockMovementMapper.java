package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockMovement;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockMovementItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    @Mapping(source = "warehouse.id", target = "warehouseId")
    @Mapping(source = "warehouse.name", target = "warehouseName")
    StockMovementDto toDto(StockMovement stockMovement);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    StockMovementDto.Item toDtoItem(StockMovementItem stockMovementItem);
}
