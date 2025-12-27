package com.apetitto.apetittoerpbackend.erp.warehouse.service;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.DashboardStockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.IncomingStockReportDto;

import java.time.Instant;
import java.util.List;

public interface DashboardService {
    List<DashboardStockItemDto> getDetailedStockValuation(List<Long> warehouseIds);

    List<IncomingStockReportDto> getIncomingStockReport(List<Long> warehouseIds, Instant dateFrom, Instant dateTo);
}
