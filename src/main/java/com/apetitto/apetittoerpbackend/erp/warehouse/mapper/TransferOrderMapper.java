package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.TransferOrderDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrder;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TransferOrderMapper {

    @Mapping(source = "sourceWarehouse.id", target = "sourceWarehouseId")
    @Mapping(source = "sourceWarehouse.name", target = "sourceWarehouseName")
    @Mapping(source = "destinationWarehouse.id", target = "destinationWarehouseId")
    @Mapping(source = "destinationWarehouse.name", target = "destinationWarehouseName")
    @Mapping(source = "items", target = "items")
    TransferOrderDto toDto(TransferOrder transferOrder);

    List<TransferOrderDto> toDtoList(List<TransferOrder> transferOrders);

    @Mapping(source = "product.id", target = "productId")
    @Mapping(source = "product.name", target = "productName")
    @Mapping(source = "product.sellingPrice", target = "sellingPrice")
    TransferOrderDto.Item toDtoItem(com.apetitto.apetittoerpbackend.erp.warehouse.model.TransferOrderItem item);
}
