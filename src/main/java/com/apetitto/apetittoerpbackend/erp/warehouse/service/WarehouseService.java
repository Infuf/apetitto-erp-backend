package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Warehouse;

import java.util.List;

public interface WarehouseService {

    WarehouseDto createWarehouse(WarehouseDto warehouseDto);

    List<WarehouseDto> getAllWarehouses();

    WarehouseDto getWarehouseById(Long id);

    WarehouseDto updateWarehouse(WarehouseDto warehouseDto);

    void deleteWarehouse(Long id);

    Warehouse findWarehouseEntityById(Long id);
}