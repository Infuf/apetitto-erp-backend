package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Операции: Склад", description = "API для выполнения складских операций...")
@RequestMapping("/api/v1/warehouse")
public interface WarehouseOperationsApi {

    @Operation(summary = "Получение остатков на складе с фильтрацией и пагинацией")
    @GetMapping("/stock")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_STORE_MANAGER','ROLE_OWNER')")
    ResponseEntity<Page<StockItemDto>> getStockByWarehouse(
            @Parameter() @RequestParam Long warehouseId,
            @Parameter() @RequestParam(required = false) String searchQuery,
            @Parameter() @RequestParam(required = false) Long categoryId,
            @Parameter() @RequestParam(defaultValue = "false") boolean showZeroQuantity,
            Pageable pageable
    );

    @Operation(summary = "Проведение складского движения", description = "Единый эндпоинт для приемки (INBOUND), списания (OUTBOUND), перемещений и корректировок (ADJUSTMENT).")
    @PostMapping("/movements")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER')")
    ResponseEntity<Void> processStockMovement(@RequestBody StockMovementRequestDto requestDto);

    @Operation(summary = "Получение истории движений на складе")
    @GetMapping("/movements/history")
    @PreAuthorize("hasAnyRole('ROLE_ADMIN','ROLE_WAREHOUSE_MANAGER','ROLE_OWNER')")
    ResponseEntity<Page<StockMovementDto>> getMovementHistory(
            @Parameter() @RequestParam Long warehouseId,
            @Parameter() @RequestParam(required = false) MovementType movementType,
            @Parameter() @RequestParam(required = false) Instant dateFrom,
            @Parameter() @RequestParam(required = false) Instant dateTo,
            Pageable pageable
    );
}