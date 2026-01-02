package com.apetitto.apetittoerpbackend.erp.hr.controller;

import com.apetitto.apetittoerpbackend.erp.hr.controller.api.EmployeeApi;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeCreateDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeResponseDto;
import com.apetitto.apetittoerpbackend.erp.hr.dto.EmployeeUpdateDto;
import com.apetitto.apetittoerpbackend.erp.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class EmployeeController implements EmployeeApi {

    private final EmployeeService employeeService;

    @Override
    public ResponseEntity<EmployeeResponseDto> createEmployee(EmployeeCreateDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(employeeService.createEmployee(dto));
    }

    @Override
    public ResponseEntity<Page<EmployeeResponseDto>> getAllEmployees(Pageable pageable) {
        return ResponseEntity.ok(employeeService.getAllEmployees(pageable));
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> getEmployeeById(Long id) {
        return ResponseEntity.ok(employeeService.getEmployeeById(id));
    }

    @Override
    public ResponseEntity<EmployeeResponseDto> updateEmployee(Long id, EmployeeUpdateDto dto) {
        return ResponseEntity.ok(employeeService.updateEmployee(id, dto));
    }

    @Override
    public ResponseEntity<Void> dismissEmployee(Long id) {
        employeeService.dismissEmployee(id);
        return ResponseEntity.ok().build();
    }
}