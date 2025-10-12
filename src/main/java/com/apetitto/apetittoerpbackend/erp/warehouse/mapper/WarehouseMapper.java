package com.apetitto.apetittoerpbackend.erp.warehouse.mapper;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;

@Mapper(componentModel = "spring")
public interface WarehouseMapper {
    WarehouseDto toDto(Warehouse warehouse);

    Warehouse toEntity(WarehouseDto warehouseDto);

    List<WarehouseDto> toDtoList(List<Warehouse> warehouses);

    void updateEntityFromDto(WarehouseDto dto, @MappingTarget Warehouse entity);
}
