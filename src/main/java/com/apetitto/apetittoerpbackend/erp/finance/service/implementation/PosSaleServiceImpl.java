package com.apetitto.apetittoerpbackend.erp.finance.service.implementation;

import com.apetitto.apetittoerpbackend.erp.finance.dto.PosSaleRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceTransactionService;
import com.apetitto.apetittoerpbackend.erp.finance.service.PosSaleService;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceOperationType.INCOME;
import static com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType.SELL;

@Service
@RequiredArgsConstructor
public class PosSaleServiceImpl implements PosSaleService {

    private final WarehouseService warehouseService;
    private final FinanceTransactionService financeTransactionService;

    @Override
    @Transactional
    public void processSale(PosSaleRequestDto request) {

        warehouseService.processStockMovement(buildMovement(request));
        financeTransactionService.createTransaction(buildTransaction(request));

    }

    private StockMovementRequestDto buildMovement(PosSaleRequestDto request) {
        var movement = new StockMovementRequestDto();
        movement.setWarehouseId(request.getWarehouseId());
        movement.setMovementType(SELL);
        movement.setComment(request.getDescription());

        var items = request.getItems().stream().map(item -> {
                    var stockItem = new StockMovementRequestDto.Item();
                    stockItem.setProductId(item.getProductId());
                    stockItem.setQuantity(item.getQuantity());
                    return stockItem;
                }
        ).toList();

        movement.setItems(items);

        return movement;
    }

    private TransactionCreateRequestDto buildTransaction(PosSaleRequestDto request) {
        var trx = new TransactionCreateRequestDto();
        trx.setAmount(request.getAmount());
        trx.setOperationType(INCOME);
        trx.setToAccountId(request.getToAccountId());
        trx.setCategoryId(request.getCategoryId());
        trx.setSubcategoryId(request.getSubcategoryId());
        trx.setDescription(request.getDescription());
        trx.setTransactionDate(request.getTransactionDate());
        trx.setItems(request.getItems());
        return trx;
    }
}
