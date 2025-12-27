package com.apetitto.apetittoerpbackend.erp.finance.controller.api;

import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.CompanyFinancialStateDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.ExpenseReportDto;
import com.apetitto.apetittoerpbackend.erp.finance.dto.dashboard.IncomeReportDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;


@Tag(name = "Аналитика: Финансы", description = "Дашборды по деньгам")
@RequestMapping("/api/v1/finance/dashboard")
public interface FinanceDashboardApi {

    @Operation(summary = "Получить сводку состояния компании",
            description = "Возвращает: 1. Сколько есть денег. 2. Сколько нам должны. 3. Сколько мы должны.")
    @GetMapping("/state")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'FINANCE_OFFICER')")
    ResponseEntity<CompanyFinancialStateDto> getCompanyState();

    @Operation(summary = "Отчёт по расходам",
            description = "Детализация расходов по категориям и типам операций за период.")
    @GetMapping("/expenses")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','FINANCE_OFFICER')")
    ResponseEntity<ExpenseReportDto> getExpenseReport(
            @RequestParam Instant dateFrom,
            @RequestParam Instant dateTo);

    @Operation(summary = "Отчет по доходам",
            description = "Детализация доходов по категориям и типам операций за период (Выручка, Оплата от дилеров).")
    @GetMapping("/income")
    @PreAuthorize("hasAnyRole('ADMIN','OWNER','FINANCE_OFFICER')")
    ResponseEntity<IncomeReportDto> getIncomeReport(
            @RequestParam Instant dateFrom,
            @RequestParam Instant dateTo);

}
