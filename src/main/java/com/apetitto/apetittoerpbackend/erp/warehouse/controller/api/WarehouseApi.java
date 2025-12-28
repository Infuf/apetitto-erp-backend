package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.WarehouseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Справочник: Склады", description = "API для управления складами")
@RequestMapping("/api/v1/warehouses")
public interface WarehouseApi {

    @Operation(summary = "Создание нового склада")
    @PostMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<WarehouseDto> createWarehouse(@RequestBody WarehouseDto warehouseDto);

    @Operation(summary = "Получение списка всех складов")
    @GetMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_FINANCE_OFFICER')")
    ResponseEntity<List<WarehouseDto>> getAllWarehouses();

    @Operation(summary = "Получение склада по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<WarehouseDto> getWarehouseById(@PathVariable Long id);

    @Operation(summary = "Обновление существующего склада", description = "Обновляет склад. ID должен быть указан в теле запроса.")
    @PutMapping
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<WarehouseDto> updateWarehouse(@RequestBody WarehouseDto warehouseDto);

    @Operation(summary = "Удаление склада по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Void> deleteWarehouse(@PathVariable Long id);
}