package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Справочник: Склады", description = "API для управления складами")
@RequestMapping("/api/v1/warehouses")
public interface WarehouseApi {

    @Operation(summary = "Создание нового склада")
    @PostMapping
    ResponseEntity<WarehouseDto> createWarehouse(@RequestBody WarehouseDto warehouseDto);

    @Operation(summary = "Получение списка всех складов")
    @GetMapping
    ResponseEntity<List<WarehouseDto>> getAllWarehouses();

    @Operation(summary = "Получение склада по ID")
    @GetMapping("/{id}")
    ResponseEntity<WarehouseDto> getWarehouseById(@PathVariable Long id);

    @Operation(summary = "Обновление существующего склада", description = "Обновляет склад. ID должен быть указан в теле запроса.")
    @PutMapping
    ResponseEntity<WarehouseDto> updateWarehouse(@RequestBody WarehouseDto warehouseDto);

    @Operation(summary = "Удаление склада по ID")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteWarehouse(@PathVariable Long id);
}