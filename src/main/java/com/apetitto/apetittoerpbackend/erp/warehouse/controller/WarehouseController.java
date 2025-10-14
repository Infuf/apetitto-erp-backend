package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.warehouse.controller.api.WarehouseApi;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class WarehouseController implements WarehouseApi {

    private final WarehouseService warehouseService;

    @Override
    public ResponseEntity<WarehouseDto> createWarehouse(WarehouseDto warehouseDto) {
        var createdWarehouse = warehouseService.createWarehouse(warehouseDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdWarehouse);
    }

    @Override
    public ResponseEntity<List<WarehouseDto>> getAllWarehouses() {
        var warehouses = warehouseService.getAllWarehouses();
        return ResponseEntity.ok(warehouses);
    }

    @Override
    public ResponseEntity<WarehouseDto> getWarehouseById(Long id) {
        var warehouse = warehouseService.getWarehouseById(id);
        return ResponseEntity.ok(warehouse);
    }

    @Override
    public ResponseEntity<WarehouseDto> updateWarehouse(WarehouseDto warehouseDto) {
        var updatedWarehouse = warehouseService.updateWarehouse(warehouseDto);
        return ResponseEntity.ok(updatedWarehouse);
    }

    @Override
    public ResponseEntity<Void> deleteWarehouse(Long id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }
}