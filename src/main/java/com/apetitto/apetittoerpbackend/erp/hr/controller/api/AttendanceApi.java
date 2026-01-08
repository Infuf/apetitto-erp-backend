package com.apetitto.apetittoerpbackend.erp.hr.controller.api;

import com.apetitto.apetittoerpbackend.erp.hr.dto.AttendanceUpdateDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "HR: Табель учета времени", description = "Управление посещаемостью")
@RequestMapping("/api/v1/hr/attendance")
public interface AttendanceApi {

    @Operation(summary = "Внести/Изменить отметку времени",
            description = "Устанавливает время прихода/ухода. " +
                    "Админы и HR могут менять всё. " +
                    "Обычные пользователи могут менять только если они Менеджеры Департамента сотрудника. " +
                    "Нельзя ставить отметки на завтрашний день.")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'USER')")
    ResponseEntity<Void> updateAttendance(@RequestBody @Valid AttendanceUpdateDto dto);
}