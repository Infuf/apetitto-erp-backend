package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.Warehouse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WarehouseService {

    WarehouseDto createWarehouse(WarehouseDto warehouseDto);

    List<WarehouseDto> getAllWarehouses();

    WarehouseDto getWarehouseById(Long id);

    WarehouseDto updateWarehouse(WarehouseDto warehouseDto);

    void deleteWarehouse(Long id);

    Warehouse findWarehouseEntityById(Long id);


    Page<StockItemDto> getStockByWarehouse(Long warehouseId, String searchQuery, Long categoryId,
                                           boolean showZeroQuantity, Pageable pageable);


    void processStockMovement(StockMovementRequestDto requestDto);
}