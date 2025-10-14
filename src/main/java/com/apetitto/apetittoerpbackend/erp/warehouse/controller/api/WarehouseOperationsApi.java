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

    @Operation(summary = "Получение остатков на складе с фильтрацией и пагинацией")
    @GetMapping("/stock")
    ResponseEntity<Page<StockItemDto>> getStockByWarehouse(
            @Parameter() @RequestParam Long warehouseId,
            @Parameter() @RequestParam(required = false) String searchQuery,
            @Parameter() @RequestParam(required = false) Long categoryId,
            @Parameter() @RequestParam(defaultValue = "false") boolean showZeroQuantity,
            Pageable pageable
    );

    @Operation(summary = "Проведение складского движения", description = "Единый эндпоинт для приемки (INBOUND), списания (OUTBOUND), перемещений и корректировок (ADJUSTMENT).")
    @PostMapping("/movements")
    ResponseEntity<Void> processStockMovement(@RequestBody StockMovementRequestDto requestDto);
}