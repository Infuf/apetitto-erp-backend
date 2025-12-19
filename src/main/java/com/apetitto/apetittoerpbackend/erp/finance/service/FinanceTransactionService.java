package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.dto.StockMovementRequestDto;
import com.apetitto.apetittoerpbackend.erp.warehouse.model.enums.MovementType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

public interface FinanceTransactionService {

    TransactionResponseDto createTransaction(TransactionCreateRequestDto request);


    Page<TransactionResponseDto> getTransactions(Long accountId, Instant dateFrom, Instant dateTo, Pageable pageable);

    void createDebtTransaction(
            Long accountId,
            List<StockMovementRequestDto.Item> items,
            MovementType movementType,
            String description);

    TransactionDetailDto getTransactionById(Long id);

    void cancelTransaction(Long id, String reason);
}