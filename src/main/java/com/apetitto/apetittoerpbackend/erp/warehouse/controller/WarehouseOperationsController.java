package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.warehouse.controller.api.WarehouseOperationsApi;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class WarehouseOperationsController implements WarehouseOperationsApi {
    private final WarehouseService warehouseService;

    @Override
    public ResponseEntity<Page<StockItemDto>> getStockByWarehouse(
            Long warehouseId,
            String searchQuery,
            Long categoryId,
            boolean showZeroQuantity,
            Pageable pageable) {

        Page<StockItemDto> stockItemsPage = warehouseService.getStockByWarehouse(
                warehouseId, searchQuery, categoryId, showZeroQuantity, pageable);

        return ResponseEntity.ok(stockItemsPage);
    }

    @Override
    public ResponseEntity<Void> processStockMovement(StockMovementRequestDto requestDto) {
        warehouseService.processStockMovement(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Override
    public ResponseEntity<Page<StockMovementDto>> getMovementHistory(Long warehouseId, MovementType movementType,
                                                                     Instant dateFrom, Instant dateTo, Pageable pageable) {
        var historyPage = warehouseService.getMovementHistory(warehouseId, movementType, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(historyPage);
    }

}
