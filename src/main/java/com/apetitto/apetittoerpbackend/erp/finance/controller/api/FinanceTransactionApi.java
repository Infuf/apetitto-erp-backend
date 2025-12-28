package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.CancellationRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionCreateRequestDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.TransactionDetailDto;
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
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER','OWNER')")
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

    @Operation(summary = "Получить детали операции",
            description = "Возвращает полную информацию о транзакции, включая список товаров (детализацию).")
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'OWNER')")
    ResponseEntity<TransactionDetailDto> getTransactionById(@PathVariable Long id);

    @Operation(summary = "Отменить транзакцию (Сторно)",
            description = "Откатывает балансы счетов. Обычные пользователи могут отменить только в течение 72 часов.")
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER', 'OWNER')")
    ResponseEntity<Void> cancelTransaction(
            @PathVariable Long id,
            @RequestBody @Valid CancellationRequestDto requestDto
    );
}