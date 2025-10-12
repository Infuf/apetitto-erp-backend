package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Операции: Склад", description = "API для выполнения складских операций...")
@RequestMapping("/api/v1/warehouse")
public interface WarehouseOperationsApi {

    @Operation(summary = "Получение остатков на складе с пагинацией")
    @Parameters({
            @Parameter(name = "page", description = "Номер страницы (начиная с 0)", example = "0"),
            @Parameter(name = "size", description = "Количество элементов на странице", example = "10"),
            @Parameter(name = "sort", description = "Сортировка (например: name,asc)", example = "name,asc")
    })
    @GetMapping("/stock")
    ResponseEntity<Page<StockItemDto>> getStockByWarehouse(
            @Parameter(description = "ID склада", required = true) @RequestParam Long warehouseId,
            Pageable pageable
    );

    @Operation(summary = "Проведение складского движения", description = "Единый эндпоинт для приемки (INBOUND) и списания (OUTBOUND) товаров.")
    @PostMapping("/movements")
    ResponseEntity<Void> processStockMovement(@RequestBody StockMovementRequestDto requestDto);
}