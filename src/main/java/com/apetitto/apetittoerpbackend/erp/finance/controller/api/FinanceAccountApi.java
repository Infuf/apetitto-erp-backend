package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.AssignOwnerDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.FinanceAccountDto;
import com.apetitto.apetittoerpbackend.erp.finance.model.enums.FinanceAccountType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Финансы: Счета", description = "Управление кошельками и контрагентами")
@RequestMapping("/api/v1/finance/accounts")
public interface FinanceAccountApi {

    @Operation(summary = "Получить список счетов (Умный фильтр)")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<List<FinanceAccountDto>> getAccounts(@RequestParam(required = false) FinanceAccountType type);

    @Operation(summary = "Получить счет по ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<FinanceAccountDto> getAccountById(@PathVariable Long id);

    @Operation(summary = "Создать счет")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<FinanceAccountDto> createAccount(@RequestBody FinanceAccountDto dto);

    @Operation(summary = "Обновить счет (название, описание)")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'FINANCE_OFFICER')")
    ResponseEntity<FinanceAccountDto> updateAccount(@PathVariable Long id, @RequestBody FinanceAccountDto dto);

    @Operation(summary = "Архивировать счет (Soft Delete)")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE_OFFICER')")
    ResponseEntity<Void> deleteAccount(@PathVariable Long id);

    @Operation(summary = "Привязать/Отвязать пользователя")
    @PutMapping("/{id}/assign-user")
    @PreAuthorize("hasRole('ADMIN')")
    ResponseEntity<FinanceAccountDto> assignUser(@PathVariable Long id, @RequestBody AssignOwnerDto dto);
}