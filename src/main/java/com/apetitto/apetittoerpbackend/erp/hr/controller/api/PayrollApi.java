package com.apetitto.apetittoerpbackend.erp.hr.controller.api;

import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollAccrualDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.payroll.PayrollRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Tag(name = "HR: Зарплата (Payroll)", description = "Управление начислениями зарплат, история и отмена")
@RequestMapping("/api/v1/hr/payroll")
@PreAuthorize("hasAnyRole('ADMIN', 'HR')")
public interface PayrollApi {

    @Operation(summary = "Рассчитать и начислить зарплату",
            description = "Запускает процесс расчета для выбранного периода и списка сотрудников (или отдела). Сразу создает транзакцию начисления в финансах.")
    @PostMapping("/calculate")
    ResponseEntity<Void> calculatePayroll(@RequestBody @Valid PayrollRequestDto request);

    @Operation(summary = "Получить историю начислений",
            description = "Поиск ведомостей с фильтрацией по дате, отделу и сотруднику.")
    @GetMapping
    ResponseEntity<Page<PayrollAccrualDto>> getPayrolls(
            @Parameter(description = "Начало периода")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Конец периода")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,

            @Parameter(description = "Фильтр по отделу")
            @RequestParam(required = false) Long departmentId,

            @Parameter(description = "Фильтр по сотруднику")
            @RequestParam(required = false) Long employeeId,

            Pageable pageable
    );

    @Operation(summary = "Получить детали начисления",
            description = "Возвращает полную информацию о расчете (ставки, часы, штрафы) по ID начисления.")
    @GetMapping("/{id}")
    ResponseEntity<PayrollAccrualDto> getPayrollById(@PathVariable Long id);

    @Operation(summary = "Отменить начисление (Сторно)",
            description = "Помечает начисление как CANCELLED и автоматически отменяет финансовую транзакцию.")
    @PostMapping("/{id}/cancel")
    ResponseEntity<Void> cancelPayroll(@PathVariable Long id);
}