package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@Tag(name = "Финансы: Операции", description = "API для управления финансовыми транзакциями (доходы, расходы, переводы)")
@RequestMapping("/api/v1/finance/transactions")
public interface FinanceTransactionApi {

    @Operation(summary = "Создать новую транзакцию",
            description = "Создает запись о финансовой операции (Приход, Расход, Перевод, Долг) и автоматически обновляет балансы соответствующих счетов.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<TransactionResponseDto> createTransaction(@Valid @RequestBody TransactionCreateRequestDto request);

    @Operation(summary = "Получить журнал транзакций",
            description = "Возвращает список операций с возможностью фильтрации по счету и дате.")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'OWNER')")
    ResponseEntity<Page<TransactionResponseDto>> getTransactions(
            @Parameter(description = "ID счета (фильтр по отправителю ИЛИ получателю)")
            @RequestParam(required = false) Long accountId,

            @Parameter(description = "Начало периода (UTC)")
            @RequestParam(required = false) Instant dateFrom,

            @Parameter(description = "Конец периода (UTC)")
            @RequestParam(required = false) Instant dateTo,

            Pageable pageable
    );
}