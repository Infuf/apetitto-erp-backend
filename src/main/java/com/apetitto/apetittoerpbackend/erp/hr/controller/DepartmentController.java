package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.controller.api.DepartmentApi;
import com.apetitto.apetittoerpbackend.erp.hr.dto.DepartmentDto;
import com.apetitto.apetittoerpbackend.erp.hr.service.DepartmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class DepartmentController implements DepartmentApi {

    private final DepartmentService departmentService;

    @Override
    public ResponseEntity<DepartmentDto> createDepartment(DepartmentDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(departmentService.createDepartment(dto));
    }

    @Override
    public ResponseEntity<DepartmentDto> updateDepartment(Long id, DepartmentDto dto) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, dto));
    }

    @Override
    public ResponseEntity<List<DepartmentDto>> getAllDepartments() {
        return ResponseEntity.ok(departmentService.getAllDepartments());
    }

    @Override
    public ResponseEntity<DepartmentDto> getDepartmentById(Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    @Override
    public ResponseEntity<Void> deleteDepartment(Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}