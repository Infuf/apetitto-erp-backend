package com.apetitto.apetittoerpbackend.erp.warehouse.service.implementation;

import com.apetitto.apetittoerpbackend.erp.commons.exeption.InvalidRequestException;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.DashboardStockItemDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.dashboard.IncomingStockReportDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.StockItem;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.StockItemRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.repository.StockMovementRepository;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType.TRANSFER_OUT;
import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private static final BigDecimal COST_THRESHOLD = BigDecimal.TEN;
    private final StockItemRepository stockItemRepository;
    private final StockMovementRepository stockMovementRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DashboardStockItemDto> getDetailedStockValuation(List<Long> warehouseIds) {

        List<StockItem> items = stockItemRepository.findAllActiveStockItems(
                (warehouseIds != null && !warehouseIds.isEmpty()) ? warehouseIds : null
        );

        return items.stream()
                .map(this::calculateItemValue)
                .sorted(comparing(DashboardStockItemDto::getCalculatedValue).reversed())
                .toList();
    }

    private DashboardStockItemDto calculateItemValue(StockItem item) {
        var quantity = item.getQuantity();
        var averageCost = item.getAverageCost();
        var sellingPrice = item.getProduct().getSellingPrice();

        if (sellingPrice == null) {
            sellingPrice = BigDecimal.ZERO;
        }

        BigDecimal calculatedValue;
        String valuationType;

        if (averageCost.compareTo(COST_THRESHOLD) > 0) {
            calculatedValue = quantity.multiply(averageCost);
            valuationType = "COST";
        } else {
            calculatedValue = quantity.multiply(sellingPrice);
            valuationType = "PRICE";
        }

        return new DashboardStockItemDto(
                item.getWarehouse().getId(),
                item.getWarehouse().getName(),
                item.getProduct().getName(),
                quantity,
                calculatedValue,
                valuationType
        );
    }

    @Override
    public List<IncomingStockReportDto> getIncomingStockReport(List<Long> warehouseIds, Instant dateFrom, Instant dateTo) {

        if (dateFrom == null || dateTo == null) {
            throw new InvalidRequestException("Dates are required");
        }

        if (warehouseIds == null || warehouseIds.isEmpty()) {
            throw new InvalidRequestException("Warehouse IS REQUIRED  PUMPKIN!");
        }

        var movements = stockMovementRepository.findIncomingMovements(warehouseIds, dateFrom, dateTo);

        var allItems = movements.stream()
                .flatMap(m -> m.getItems().stream())
                .toList();

        var groupedDate = allItems.stream()
                .collect(groupingBy(
                        item -> item.getMovement().getWarehouse().getName(),
                        groupingBy(
                                item -> item.getProduct().getName()
                        )
                ));

        List<IncomingStockReportDto> report = new ArrayList<>();
        for (var warehouseEntry : groupedDate.entrySet()) {
            var warehouseName = warehouseEntry.getKey();
            var productMap = warehouseEntry.getValue();

            for (var productEntry : productMap.entrySet()) {
                var productName = productEntry.getKey();
                var items = productEntry.getValue();

                var product = items.get(0).getProduct();
                var totalQuantity = items.stream().map(item ->
                {
                    var movementType = item.getMovement().getMovementType();
                    var quantity = item.getQuantity();
                    if (TRANSFER_OUT.equals(movementType)) {
                        return quantity.negate();
                    }
                    return quantity;
                }).reduce(BigDecimal.ZERO, BigDecimal::add);

                if (totalQuantity.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }

                var pricePerUnit = product.getSellingPrice() != null ? product.getSellingPrice() : BigDecimal.ZERO;

                var totalAmount = totalQuantity.multiply(pricePerUnit);
                report.add(new IncomingStockReportDto(
                        warehouseName,
                        productName,
                        totalQuantity,
                        pricePerUnit,
                        totalAmount
                ));
            }
        }
        report.sort(comparing(IncomingStockReportDto::getTotalAmount).reversed());
        return report;
    }

}
