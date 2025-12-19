package com.apetitto.apetittoerpbackend.erp.finance.controller;

import com.apetitto.apetittoerpbackend.erp.finance.controller.api.FinanceTransactionApi;
import com.apetitto.apetittoerpbackend.erp.finance.dto.CancellationRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import com.apetitto.apetittoerpbackend.erp.finance.service.FinanceTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;

@RestController
@RequiredArgsConstructor
public class FinanceTransactionController implements FinanceTransactionApi {

    private final FinanceTransactionService transactionService;

    @Override
    public ResponseEntity<TransactionResponseDto> createTransaction(TransactionCreateRequestDto request) {
        TransactionResponseDto createdTransaction = transactionService.createTransaction(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }

    @Override
    public ResponseEntity<Page<TransactionResponseDto>> getTransactions(Long accountId,
                                                                        Instant dateFrom,
                                                                        Instant dateTo,
                                                                        Pageable pageable) {
        Page<TransactionResponseDto> transactions = transactionService.getTransactions(accountId, dateFrom, dateTo, pageable);
        return ResponseEntity.ok(transactions);
    }

    @Override
    public ResponseEntity<TransactionDetailDto> getTransactionById(Long id) {
        return ResponseEntity.ok(transactionService.getTransactionById(id));
    }

    @Override
    public ResponseEntity<Void> cancelTransaction(Long id, CancellationRequestDto requestDto) {
        transactionService.cancelTransaction(id, requestDto.getReason());
        return ResponseEntity.ok().build();
    }
}