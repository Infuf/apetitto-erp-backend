package com.apetitto.apetittoerpbackend.erp.warehouse.controller.api;

import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.DashboardStockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.IncomingStockReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.List;

@Tag(name = "Аналитика: Склад", description = "API для получения агрегированных данных и отчетов по складу")
@RequestMapping("/api/v1/warehouse/dashboard")
public interface WarehouseDashboardApi {

    @Operation(
            summary = "Детальная оценка остатков для графиков",
            description = "Возвращает список всех товаров с остатком > 0 и их оценочной стоимостью. " +
                    "Логика расчета: если себестоимость > 10 сум, считается по себестоимости (COST), " +
                    "иначе — по цене продажи (PRICE). Результат отсортирован от самых дорогих к дешевым."
    )
    @GetMapping("/stock-valuation-details")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'FINANCE_OFFICER')")
    ResponseEntity<List<DashboardStockItemDto>> getDetailedStockValuation(
            @Parameter(description = "Список ID складов для фильтрации (если пустой — берем все склады)")
            @RequestParam(required = false) List<Long> warehouseIds
    );

    @Operation(summary = "Отчет о поступлениях в магазины",
            description = "Показывает, какие товары и на какую сумму (в розничных ценах) поступили на склады за период.")
    @GetMapping("/incoming-report")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'FINANCE_OFFICER')")
    ResponseEntity<List<IncomingStockReportDto>> getIncomingStockReport(
            @RequestParam(required = false) List<Long> destinationWarehouseIds,
            @RequestParam Instant dateFrom,
            @RequestParam Instant dateTo
    );

}