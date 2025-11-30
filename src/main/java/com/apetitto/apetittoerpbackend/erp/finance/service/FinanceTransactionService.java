package com.apetitto.apetittoerpbackend.erp.finance.service;

import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface FinanceTransactionService {

    TransactionResponseDto createTransaction(TransactionCreateRequestDto request);


    Page<TransactionResponseDto> getTransactions(Long accountId, Instant dateFrom, Instant dateTo, Pageable pageable);

    // void cancelTransaction(Long id);
}