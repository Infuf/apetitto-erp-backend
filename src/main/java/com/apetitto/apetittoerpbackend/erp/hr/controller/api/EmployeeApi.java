package com.apetitto.apetittoerpbackend.erp.hr.controller.api;

import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "HR: Сотрудники", description = "Управление персоналом (Найм, Редактирование, Увольнение)")
@RequestMapping("/api/v1/hr/employees")
public interface EmployeeApi {

    @Operation(summary = "Нанять сотрудника",
            description = "Создает User, Employee и FinanceAccount. Роль по умолчанию - USER. Часы считаются из графика.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    ResponseEntity<EmployeeResponseDto> createEmployee(@RequestBody @Valid EmployeeCreateDto dto);

    @Operation(summary = "Получить список сотрудников")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<Page<EmployeeResponseDto>> getAllEmployees(Pageable pageable);

    @Operation(summary = "Получить карточку сотрудника")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<EmployeeResponseDto> getEmployeeById(@PathVariable Long id);

    @Operation(summary = "Обновить данные сотрудника")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR')")
    ResponseEntity<EmployeeResponseDto> updateEmployee(@PathVariable Long id, @RequestBody @Valid EmployeeUpdateDto dto);

    @Operation(summary = "Уволить сотрудника (Архивация)",
            description = "Деактивирует сотрудника, его учетную запись и финансовый счет.")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    ResponseEntity<Void> dismissEmployee(@PathVariable Long id);
}