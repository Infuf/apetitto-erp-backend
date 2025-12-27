package com.apetitto.apetittoerpbackend.erp.warehouse.controller;

import com.apetitto.apetittoerpbackend.erp.warehouse.controller.api.WarehouseDashboardApi;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.DashboardStockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.IncomingStockReportDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class WarehouseDashboardController implements WarehouseDashboardApi {

    private final DashboardService dashboardService;

    @Override
    public ResponseEntity<List<DashboardStockItemDto>> getDetailedStockValuation(List<Long> warehouseIds) {
        List<DashboardStockItemDto> report = dashboardService.getDetailedStockValuation(warehouseIds);
        return ResponseEntity.ok(report);
    }

    @Override
    public ResponseEntity<List<IncomingStockReportDto>> getIncomingStockReport(List<Long> destinationWarehouseIds, Instant dateFrom, Instant dateTo) {
        return ResponseEntity.ok(dashboardService.getIncomingStockReport(destinationWarehouseIds, dateFrom, dateTo));
    }
}