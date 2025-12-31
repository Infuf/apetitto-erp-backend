package com.apetitto.apetittoerpbackend.erp.hr.controller.api;

import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "HR: Департаменты", description = "Управление структурой компании")
@RequestMapping("/api/v1/hr/departments")
public interface DepartmentApi {

    @Operation(summary = "Создать департамент")
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    ResponseEntity<DepartmentDto> createDepartment(@RequestBody @Valid DepartmentDto dto);

    @Operation(summary = "Обновить департамент")
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    ResponseEntity<DepartmentDto> updateDepartment(@PathVariable Long id, @RequestBody @Valid DepartmentDto dto);

    @Operation(summary = "Получить все департаменты")
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<List<DepartmentDto>> getAllDepartments();

    @Operation(summary = "Получить департамент по ID")
    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    ResponseEntity<DepartmentDto> getDepartmentById(@PathVariable Long id);

    @Operation(summary = "Удалить департамент")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','HR')")
    ResponseEntity<Void> deleteDepartment(@PathVariable Long id);
}