package com.apetitto.apetittoerpbackend.erp.hr.controller.api;

import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.AttendanceGridResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.EmployeeExtendedDetailDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.dashboard.SingleAttendanceRecordDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Tag(name = "HR: Дашборд и Аналитика", description = "Агрегированные данные для табелей, графиков и карточек сотрудников")
@RequestMapping("/api/v1/hr/dashboard")
public interface AttendanceDashboardApi {

    @Operation(summary = "1. Табель-сетка (Heatmap)",
            description = "Возвращает сводную таблицу посещаемости по всему департаменту. " +
                    "Используется для рисования цветных кружочков (присутствовал, опоздал, прогул). " +
                    "Не содержит финансовых данных.")
    @GetMapping("/grid")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'USER')")
    ResponseEntity<AttendanceGridResponseDto> getAttendanceGrid(
            @Parameter(description = "ID департамента (Обязательно для Админа, для Менеджера берется автоматически)")
            @RequestParam(required = false) Long departmentId,

            @Parameter(description = "Начало периода (ГГГГ-ММ-ДД)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Конец периода (ГГГГ-ММ-ДД)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    );

    @Operation(summary = "2. Детальная карточка сотрудника (Время + Деньги)",
            description = "Возвращает полную статистику: список всех смен, опозданий и список полученных авансов/штрафов. " +
                    "Используется для анализа перед выплатой зарплаты.")
    @GetMapping("/employee/{employeeId}/details")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'USER')")
    ResponseEntity<EmployeeExtendedDetailDto> getEmployeeDetails(
            @PathVariable Long employeeId,

            @Parameter(description = "Начало периода")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,

            @Parameter(description = "Конец периода")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo
    );

    @Operation(summary = "3. Данные одной смены (Для модального окна)",
            description = "Возвращает данные конкретного дня для редактирования. " +
                    "Возвращает DTO даже если записи в БД нет (пустой шаблон с плановым графиком).")
    @GetMapping("/attendance-record")
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'USER')")
    ResponseEntity<SingleAttendanceRecordDto> getSingleRecord(
            @Parameter(description = "ID сотрудника")
            @RequestParam Long employeeId,

            @Parameter(description = "Дата смены")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    );
}